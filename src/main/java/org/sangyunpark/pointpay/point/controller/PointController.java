package org.sangyunpark.pointpay.point.controller;

import org.sangyunpark.pointpay.point.service.PointService;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/point")
public class PointController {

    private final PointService pointService;

    public PointController(final PointService pointService) {
        this.pointService = pointService;
    }
}
