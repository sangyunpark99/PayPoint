package org.sangyunpark.pointpay.domain.point.cache.loader;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class PointHistoryDetailDbLoader {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoriesPerMonthResponse loadFromDb(PointHistoryDetailCacheKey key) {
        long userId = key.getUserId();
        YearMonth yearMonth = key.getYearMonth();

        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        return new PointHistoriesPerMonthResponse(pointHistoryJpaRepository.findAllByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd));
    }
}
