package org.sangyunpark.pointpay.domain.point.controller;

import jakarta.validation.Valid;
import org.sangyunpark.pointpay.domain.point.dto.request.PointAddRequest;
import org.sangyunpark.pointpay.domain.point.service.PointService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/point")
public class PointController {

    private final PointService pointService;

    public PointController(final PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/charge")
    public void chargePoint(@RequestBody @Valid final PointAddRequest request) {
        pointService.chargePoint(request.getUserId(), request.getPoint());
    }
}
