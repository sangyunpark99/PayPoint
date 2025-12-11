package org.sangyunpark.pointpay.domain.point.controller;

import jakarta.validation.Valid;
import org.sangyunpark.pointpay.domain.point.controller.dto.request.PointAddRequest;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesPerMonthResponse;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesSummaryResponse;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointService pointService;

    public PointController(final PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/charge")
    public void chargePoint(@RequestBody @Valid PointAddRequest request) {
        pointService.chargePoint(request.getUserId(), request.getPoint());
    }

    @GetMapping("/{userId}/histories")
    public PointHistoriesSummaryResponse getPointHistory(@PathVariable long userId, @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return pointService.getPointHistoriesSummary(userId, startDate, endDate);
    }

    @GetMapping("/{userId}/detail")
    public PointHistoriesPerMonthResponse getPointHistoryDetail(@PathVariable long userId, @RequestParam LocalDateTime startDate) {
        return pointService.getPointHistoryDetailPerMonth(userId, startDate);
    }
}
