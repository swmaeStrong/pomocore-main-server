package com.swmStrong.demo.domain.userSubscription.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionReq;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor // Lombok
@RequestMapping("/users/subscriptions")
@Tag(name = "유저 구독")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @Operation(
            summary = "서버에 구독한 플랜 정보와 빌링 키를 전달한다.",
            description =
                    "<p> 포트원 SDK를 이용해서 빌링키 발급을 한 후에 </p>" +
                    "<p> 구독한 플랜 정보와 빌링 키를 전달한다. </p>"
    )
    @PostMapping("")
    ResponseEntity<ApiResponse<Void>> createUserSubscription(@RequestBody UserSubscriptionReq req) {
        userSubscriptionService.createUserSubscription(req.userId(), req.subscriptionPlanId(), req.billingKey());

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }


    @PutMapping("/current")
    ResponseEntity<ApiResponse<Void>> cancelCurrentSubscription(String userSubscriptionId, String reason) {
        userSubscriptionService.cancelCurrentSubscription(userSubscriptionId, reason);

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }

    @DeleteMapping("/scheduled")
    ResponseEntity<ApiResponse<Void>> cancelScheduledSubscription(String userSubscriptionId) {
        userSubscriptionService.cancelScheduledSubscription(userSubscriptionId);

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }



}
