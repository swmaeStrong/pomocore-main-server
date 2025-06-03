package com.swmStrong.demo.domain.subscriptionPlan.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanReq;
import com.swmStrong.demo.domain.subscriptionPlan.dto.req.SubscriptionPlanRes;
import com.swmStrong.demo.domain.subscriptionPlan.service.SubscriptionPlanService;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Operation(
            security = @SecurityRequirement(name = "bearerAuth"),
            summary = "이용할 수 있는 구독 플랜들을 조회한다.",
            description =
                    "<p> 이용할 수 있는 구독 플랜들을 조회한다. </p>" +
                    "<p> 유저가 현재 구매 가능한 구독 플랜들을 조회한다. </p>"
    )
    @GetMapping("/subscription-plans")
    ResponseEntity<ApiResponse<List<SubscriptionPlanRes>>> getSubscriptionPlans() {

        List<SubscriptionPlanRes> subscriptionPlanResList =
                subscriptionPlanService.getSubscriptionPlans();

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, subscriptionPlanResList));
    }

}
