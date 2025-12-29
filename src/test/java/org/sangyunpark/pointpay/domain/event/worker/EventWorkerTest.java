package org.sangyunpark.pointpay.domain.event.worker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.pointpay.common.CommonTest;
import org.sangyunpark.pointpay.domain.event.EventWorker;
import org.sangyunpark.pointpay.domain.event.entity.EventStatus;
import org.sangyunpark.pointpay.domain.event.entity.PointHistoryEvent;
import org.sangyunpark.pointpay.domain.event.repository.PointHistoryEventJpaRepository;
import org.sangyunpark.pointpay.domain.event.service.PointHistoryEventService;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJdbcRepository;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

class EventWorkerTest extends CommonTest {

    @Autowired
    private EventWorker eventWorker;

    @Autowired
    private PointHistoryEventJpaRepository pointHistoryEventJpaRepository;

    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @SpyBean
    private PointHistoryJdbcRepository pointHistoryJdbcRepository;

    @SpyBean
    private PointHistoryEventService pointHistoryEventService;

    @BeforeEach
    void clear() {
        dbCleaner.clear();
    }

    @Test
    @DisplayName("batchInsert 이후 예외가 나면 point_history도 같이 롤백되어야 한다")
    void whenInsertException_thenPointHistoryRollbackWith() {
        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);
        PointHistoryEvent pointHistoryEvent = PointHistoryEvent.builder().userId(savedUser.getId()).balanceBefore(100).balanceAfter(200).pointType(PointType.CHARGE).createdAt(LocalDateTime.now()).build();
        PointHistoryEvent e = pointHistoryEventJpaRepository.save(pointHistoryEvent);
        assertThat(e.getEventStatus()).isEqualTo(EventStatus.READY);

        doAnswer(inv -> {
            inv.callRealMethod();
            throw new RuntimeException("ex");
        }).when(pointHistoryJdbcRepository).batchInsert(anyList());

        // when
        assertThatThrownBy(() -> eventWorker.start())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ex");

        // then
        assertThat(pointHistoryJpaRepository.count()).isEqualTo(0);

        PointHistoryEvent reloaded = pointHistoryEventJpaRepository.findById(e.getId()).orElseThrow();
        assertThat(reloaded.getEventStatus()).isEqualTo(EventStatus.READY);
    }

    @Test
    @DisplayName("정상 처리되면 point_history가 저장되고 이벤트 상태는 FINISH로 변경된다")
    void whenSuccess_thenPointHistoryInserted_andEventFinished() {
        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);

        PointHistoryEvent event = PointHistoryEvent.builder()
                .userId(savedUser.getId())
                .balanceBefore(100)
                .balanceAfter(200)
                .pointType(PointType.CHARGE)
                .createdAt(LocalDateTime.now())
                .build();

        PointHistoryEvent savedEvent = pointHistoryEventJpaRepository.save(event);
        assertThat(savedEvent.getEventStatus()).isEqualTo(EventStatus.READY);

        // when
        int processed = eventWorker.start();

        // then
        assertThat(processed).isEqualTo(1);
        assertThat(pointHistoryJpaRepository.count()).isEqualTo(1);

        PointHistoryEvent reloaded = pointHistoryEventJpaRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(reloaded.getEventStatus()).isEqualTo(EventStatus.FINISH);
    }

    @Test
    @DisplayName("point_history insert까지는 성공했지만 FINISH 전환에서 예외가 나면 point_history는 롤백되고 이벤트는 READY로 복구된다")
    void whenFinishUpdateException_thenRollbackPointHistory_andEventBackToReady() {
        // given
        User user = new User("test", 0);
        User savedUser = userJpaRepository.save(user);

        PointHistoryEvent event = PointHistoryEvent.builder()
                .userId(savedUser.getId())
                .balanceBefore(100)
                .balanceAfter(200)
                .pointType(PointType.CHARGE)
                .createdAt(LocalDateTime.now())
                .build();

        PointHistoryEvent savedEvent = pointHistoryEventJpaRepository.save(event);
        Long eventId = savedEvent.getId();

        // FINISH 전환 시점에만 예외 터트린다.
        doAnswer(inv -> {
            EventStatus from = inv.getArgument(0);
            EventStatus to = inv.getArgument(1);
            List<Long> ids = inv.getArgument(2);

            if (from == EventStatus.PROCESSING && to == EventStatus.FINISH && ids.contains(eventId)) {
                inv.callRealMethod(); // UPDATE 시도
                throw new RuntimeException("finish-ex");
            }
            return inv.callRealMethod();
        }).when(pointHistoryEventService).updateEventStatusInIds(
                eq(EventStatus.PROCESSING),
                eq(EventStatus.FINISH),
                argThat(ids -> ids != null && ids.contains(eventId))
        );

        // when
        assertThatThrownBy(() -> eventWorker.start())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("finish-ex");

        // then: insert는 같은 트랜잭션이므로 롤백
        assertThat(pointHistoryJpaRepository.count()).isEqualTo(0);

        // then: PROCESSING -> READY로 복구
        PointHistoryEvent reloaded = pointHistoryEventJpaRepository.findById(eventId).orElseThrow();
        assertThat(reloaded.getEventStatus()).isEqualTo(EventStatus.READY);
    }
}