package org.sangyunpark.pointpay.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventWakeupListener {

    private final EventWorkerSignal signal;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(PointHistoryEventCreated event) {
        log.info("PointHistoryEventCreated listen");
        signal.wakeUp();
    }
}
