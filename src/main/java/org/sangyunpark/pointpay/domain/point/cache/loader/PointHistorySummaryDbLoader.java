package org.sangyunpark.pointpay.domain.point.cache.loader;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.local.key.PointHistorySummaryCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.point.repository.projection.PointHistoryProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PointHistorySummaryDbLoader {

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoriesSummaryResponse loadFromDb(PointHistorySummaryCacheKey key) {
        List<PointHistoryProjection> rows = pointHistoryJpaRepository.findByDate(key.getUserId(), key.getStartDate(), key.getEndDate());
        List<PointHistoriesSummaryResponse.PointHistorySummaryResponse> response = rows.stream().map(r -> new PointHistoriesSummaryResponse.PointHistorySummaryResponse(r.getIncome(), r.getExpense(), r.getHistoryDate())).toList();
        return new PointHistoriesSummaryResponse(response);
    }
}
