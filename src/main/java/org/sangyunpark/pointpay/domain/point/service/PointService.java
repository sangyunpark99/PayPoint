package org.sangyunpark.pointpay.domain.point.service;

import lombok.extern.slf4j.Slf4j;
import org.sangyunpark.pointpay.domain.event.service.PointEventService;
import org.sangyunpark.pointpay.domain.point.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointStatus;
import org.sangyunpark.pointpay.domain.point.entity.PointType;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJdbcRepository;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.sangyunpark.pointpay.domain.user.entity.User;
import org.sangyunpark.pointpay.domain.user.repository.UserJpaRepository;
import org.sangyunpark.pointpay.error.BusinessException;
import org.sangyunpark.pointpay.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

;

@Slf4j
@Service
public class PointService {

    private static final int MAX_POINT = 2_100_000_000;
    private static final String INCOME = "INCOME";
    private static final String EXPENSE = "EXPENSE";

    private final UserJpaRepository userJpaRepository;
    private final PointHistoryJdbcRepository pointHistoryJdbcRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;
    private final PointEventService pointEventService;

    public PointService(UserJpaRepository userJpaRepository,
                        PointEventService pointEventService,
                        PointHistoryJdbcRepository pointHistoryJdbcRepository,PointHistoryJpaRepository pointHistoryJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.pointEventService = pointEventService;
        this.pointHistoryJdbcRepository = pointHistoryJdbcRepository;
        this.pointHistoryJpaRepository = pointHistoryJpaRepository;
    }

    @Transactional
    public void chargePoint(long userId, int pointAmount) {
        User user = userJpaRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.validatePointCharge(pointAmount, MAX_POINT);
        userJpaRepository.increasePoint(userId, pointAmount, MAX_POINT);
        pointEventService.savePointEvent(userId, pointAmount, PointType.CHARGE);
    }

    @Transactional(readOnly=true)
    public PointHistoriesResponse getPointHistories(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<PointHistory> pointHistories = pointHistoryJpaRepository.findByDate(userId, startDate, endDate);

        long incomeSum = 0L;
        long expenseSum = 0L;

        if(!pointHistories.isEmpty()) {
            HashMap<String, Long> incomeAndExpenseSum = getPointSum(pointHistories);
            incomeSum = incomeAndExpenseSum.get(INCOME);
            expenseSum = incomeAndExpenseSum.get(EXPENSE);
        }

        return PointHistoriesResponse.builder()
                .income(incomeSum)
                .expense(expenseSum)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private HashMap<String, Long> getPointSum(List<PointHistory> pointHistories) {
        long totalIncomeSum = 0L;
        long totalExpenseSum = 0L;

        for(PointHistory history : pointHistories) {
            if(PointType.CHARGE.equals(history.getPointType())) {
                totalIncomeSum += history.getPointBalance();
            }else {
                totalIncomeSum += history.getPointBalance();
            }
        }

        HashMap<String, Long> incomeAndExpenseSum = new HashMap<>();
        incomeAndExpenseSum.put(INCOME, totalIncomeSum);
        incomeAndExpenseSum.put(EXPENSE, totalExpenseSum);

        return incomeAndExpenseSum;
    }

    @Transactional
    public void savePointHistories(List<PointHistory> pointHistories) {
        pointHistoryJdbcRepository.batchInsert(pointHistories);
    }
}