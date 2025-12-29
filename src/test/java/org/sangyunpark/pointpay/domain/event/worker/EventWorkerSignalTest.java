package org.sangyunpark.pointpay.domain.event.worker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.domain.event.EventWorker;
import org.sangyunpark.pointpay.domain.event.EventWorkerSignal;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventJpaRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventWorkerSignalTest {
    static class CountingExecutor implements Executor {
        final AtomicInteger submitted = new AtomicInteger();
        final Executor delegate;

        CountingExecutor(Executor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable command) {
            submitted.incrementAndGet();
            delegate.execute(command);
        }
    }

    @Test
    @DisplayName("wakeUp을 100번 동시 호출해도 runLoop는 1번만 실행됩니다.")
    void whenCallWakeUpOneHundred_thenSummitOnce() throws Exception {

        // given
        EventWorker worker = mock(EventWorker.class);
        PointHistoryEventJpaRepository repo = mock(PointHistoryEventJpaRepository.class);

        when(repo.existsByEventStatus(any())).thenReturn(false);

        CountDownLatch workerEntered = new CountDownLatch(1);
        CountDownLatch releaseWorker = new CountDownLatch(1);

        when(worker.start()).thenAnswer(inv -> {
            workerEntered.countDown();
            releaseWorker.await(200, TimeUnit.MILLISECONDS); // 잠깐만 버팀
            return 0; // 작업 없다고 보고
        });

        CountingExecutor executor = new CountingExecutor(Executors.newCachedThreadPool());

        EventWorkerSignal signal = new EventWorkerSignal(worker, repo, executor);

        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        // when
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    signal.wakeUp();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();

        assertThat(workerEntered.await(1, TimeUnit.SECONDS)).isTrue();

        done.await();
        pool.shutdown();

        releaseWorker.countDown();

        assertThat(executor.submitted.get()).isEqualTo(1);
    }

    static class RejectOnceThenRunInlineExecutor implements Executor {
        final AtomicInteger executeCalls = new AtomicInteger(0);

        @Override
        public void execute(Runnable command) {
            int c = executeCalls.incrementAndGet();
            if (c == 1) {
                throw new RejectedExecutionException("boom (first submit rejected)");
            }

            command.run();
        }
    }

    static class EventWorkerSignal_NoCatch {
        private final Runnable runLoop;
        private final Executor executor;

        private final Object monitor = new Object();
        private boolean running = false;
        private boolean signaled = false;

        EventWorkerSignal_NoCatch(Runnable runLoop, Executor executor) {
            this.runLoop = runLoop;
            this.executor = executor;
        }

        public void wakeUp() {
            boolean shouldStart = false;
            synchronized (monitor) {
                signaled = true;
                if (!running) {
                    running = true;
                    shouldStart = true;
                }
            }

            if (!shouldStart) return;

            java.util.concurrent.CompletableFuture.runAsync(runLoop, executor);
        }
    }

    @Test
    void whenFailedSubmitRunAsync_thenRunningIsTrueNotStartWorker(){
        // given
        AtomicInteger runLoopCalls = new AtomicInteger(0);

        RejectOnceThenRunInlineExecutor executor = new RejectOnceThenRunInlineExecutor();
        EventWorkerSignal_NoCatch signal = new EventWorkerSignal_NoCatch(
                runLoopCalls::incrementAndGet,
                executor
        );

        // when
        assertThatThrownBy(signal::wakeUp)
                .isInstanceOf(RejectedExecutionException.class);

        // then
        assertThat(runLoopCalls.get()).isEqualTo(0);

        // when
        signal.wakeUp();

        // then
        assertThat(executor.executeCalls.get()).isEqualTo(1);
        assertThat(runLoopCalls.get()).isEqualTo(0);
    }

    @Test
    @DisplayName("submit이 1번 reject 되더라도 running이 복구되어 다음 wakeUp에서 다시 실행됩니다.")
    void whenRejectOnce_thenRecoverRunning_and_nextWakeUpStartsRunLoop() {
        // given
        EventWorker worker = mock(EventWorker.class);
        PointHistoryEventJpaRepository repo = mock(PointHistoryEventJpaRepository.class);
        when(repo.existsByEventStatus(any())).thenReturn(false);

        CountDownLatch entered = new CountDownLatch(1);
        when(worker.start()).thenAnswer(inv -> {
            entered.countDown();
            return 0;
        });

        RejectOnceThenRunInlineExecutor executor = new RejectOnceThenRunInlineExecutor();
        EventWorkerSignal signal = new EventWorkerSignal(worker, repo, executor);

        signal.wakeUp();

        assertThat(entered.getCount()).isEqualTo(1);
        Boolean runningAfterFirst = (Boolean) ReflectionTestUtils.getField(signal, "running");
        assertThat(runningAfterFirst).isFalse();

        signal.wakeUp();

        // then
        assertThat(entered.getCount()).isEqualTo(0);
        assertThat(executor.executeCalls.get()).isEqualTo(2);
        Boolean runningAfterSecond = (Boolean) ReflectionTestUtils.getField(signal, "running");
        assertThat(runningAfterSecond).isFalse();
    }
}