package org.sangyunpark.pointpay.domain.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventJpaRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventWorkerSignal {

    private final EventWorker worker;
    private final PointHistoryEventJpaRepository pointHistoryEventJpaRepository;
    private final Executor eventWorkerExecutor;

    private final Object monitor = new Object();
    private boolean running = false;
    private boolean signaled = false; // 워커 실행중 새로운 작업

    public void wakeUp() {
        boolean shouldStart = false;
        synchronized (monitor) { //
            signaled = true;

            if (!running) { //
                running = true;
                shouldStart = true;
            }
        }

        if (shouldStart) {
            try {
                CompletableFuture
                        .runAsync(this::runLoop, eventWorkerExecutor)
                        .whenComplete((v, ex) -> {
                            if (ex != null) log.error("EventWorker failed", ex);
                        });
            } catch (RejectedExecutionException ex) {
                log.error("EventWorker submit rejected", ex);
                synchronized(monitor) {
                    running = false;
                }
            }
        }
    }

    private void runLoop() {
        try {
            while (true) {
                synchronized (monitor) {
                    signaled = false;
                }

                int processed;
                do {
                    processed = worker.start();
                } while (processed > 0);

                synchronized (monitor) {
                    if (signaled || hasWork()) {
                        continue;
                    }

                    running = false;
                    return;
                }
            }
        } catch (Throwable ex) {
            boolean restart;
            synchronized (monitor) {
                restart = signaled || hasWork();
                running = false;
            }
            log.error("EventWorker failed", ex);

            if (restart) {
                wakeUp();
            }
        }
    }

    public boolean hasWork() {
        return pointHistoryEventJpaRepository.existsByEventStatus(EventStatus.READY);
    }
}
