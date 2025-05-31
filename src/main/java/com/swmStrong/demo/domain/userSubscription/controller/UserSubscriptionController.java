package com.swmStrong.demo.domain.userSubscription.controller;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.domain.userSubscription.dto.UserSubscriptionReq;
import com.swmStrong.demo.domain.userSubscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor // Lombok
@RequestMapping("/users")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    @Operation(
            summary = "서버에 구독한 플랜 정보와 빌링 키를 전달한다.",
            description =
                    "<p> 포트원 SDK를 이용해서 빌링키 발급을 한 후에 </p>" +
                    "<p> 구독한 플랜 정보와 빌링 키를 전달한다. </p>"
    )
    @PostMapping("/subscriptions")
    ResponseEntity<ApiResponse<Void>> createUserSubscription(@RequestBody UserSubscriptionReq req) {
        userSubscriptionService.createUserSubscription(req.userId(), req.subscriptionPlanId(), req.billingKey());

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }


    @DeleteMapping("/subscriptions")
    ResponseEntity<ApiResponse<Void>> deleteUserSubscription(@RequestBody UserSubscriptionReq req) {
        userSubscriptionService.createUserSubscription(req.userId(), req.subscriptionPlanId(), req.billingKey());

        return ResponseEntity
                .status(SuccessCode._OK.getHttpStatus())
                .body(ApiResponse.success(SuccessCode._OK, null));
    }

}
