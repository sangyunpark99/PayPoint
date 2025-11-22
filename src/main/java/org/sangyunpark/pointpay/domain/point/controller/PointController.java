package org.sangyunpark.pointpay.domain.point.controller;

import jakarta.validation.Valid;
import org.sangyunpark.pointpay.domain.point.dto.request.PointAddRequest;
import org.sangyunpark.pointpay.domain.point.dto.response.PointBalanceResponse;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/point")
public class PointController {

    private final PointService pointService;

    public PointController(final PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/charge")
    public void chargePoint(@RequestBody @Valid final PointAddRequest request) {
        pointService.chargePoint(request.getUserId(), request.getPoint());
    }

    @GetMapping("/{userId}")
    public PointBalanceResponse getPointBalance(@PathVariable final long userId) {
        return pointService.getPointBalance(userId);
    }
}
