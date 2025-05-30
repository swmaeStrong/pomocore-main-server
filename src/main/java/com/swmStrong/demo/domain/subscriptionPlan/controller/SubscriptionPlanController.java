package com.swmStrong.demo.domain.subscriptionPlan.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.entity.SubscriptionPlan;
import com.swmStrong.demo.domain.subscriptionPlan.service.SubscriptionPlanService;
import com.swmStrong.demo.domain.userSubscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(SubscriptionService subscriptionService, SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @Operation(
            summary = "새로운 구독 플랜을 추가한다.",
            description =
                    "<p> 새로운 구독 플랜을 추가한다 </p>" +
                    "<p> 구독 플랜, 기간, 가격을 설정한다 </p>"
    )
    @PostMapping("/subscription-plans")
    ResponseEntity<ApiResponse<Void>> createSubscriptionPlan(@RequestBody SubscriptionPlanReq SubscriptionPlanReq) {

        subscriptionPlanService.addSubscriptionPlan(SubscriptionPlanReq);

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }
}
