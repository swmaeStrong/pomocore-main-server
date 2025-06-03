package com.swmStrong.demo.domain.subscriptionPlan.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.service.SubscriptionPlanService;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "구독 플랜")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(UserSubscriptionService userSubscriptionService, SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "새로운 구독 플랜을 추가한다.",
            description =
                    "<p> 새로운 구독 플랜을 추가한다 </p>" +
                    "<p> 구독 플랜, 기간, 가격을 설정한다 </p>"
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/subscription-plans")
    ResponseEntity<ApiResponse<Void>> createSubscriptionPlan(@RequestBody SubscriptionPlanReq SubscriptionPlanReq) {

        subscriptionPlanService.addSubscriptionPlan(SubscriptionPlanReq);

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }
}
