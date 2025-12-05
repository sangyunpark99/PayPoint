package org.sangyunpark.pointpay.domain.point.repository.projection;

import java.time.LocalDate;

public interface PointHistoryProjection {
    long getIncome();
    long getExpense();
    LocalDate getHistoryDate();
}
