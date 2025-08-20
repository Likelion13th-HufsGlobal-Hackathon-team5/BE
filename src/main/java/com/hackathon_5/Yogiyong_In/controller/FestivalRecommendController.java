package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.ApiResponse;
import com.hackathon_5.Yogiyong_In.DTO.AiRecommend.FestivalRecommendGetResDto;
import com.hackathon_5.Yogiyong_In.service.FestivalRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festivals")
public class FestivalRecommendController {

    private final FestivalRecommendService recommendService;

    @Operation(
            summary = "AI 축제 추천",
            description = "로그인한 사용자의 선택 키워드를 기반으로 축제 ID를 AI가 선택하고, 해당 축제 정보를 반환합니다. (reason 제외)",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<FestivalRecommendGetResDto>> recommend(
            Authentication authentication,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
    ) {
        String userId = authentication.getName(); // JwtAuthenticationFilter에서 세팅됨
        var result = recommendService.recommend(userId, limit);

        var body = ApiResponse.ok(result.data()); // <-- data만 바디에

        // message가 있으면 헤더에 포함 (프론트에서 선택적으로 표시)
        if (result.message() == null || result.message().isBlank()) {
            return ResponseEntity.ok(body);
        } else {
            return ResponseEntity.ok()
                    .header("X-Info-Message", result.message())
                    .body(body);
        }
    }
}
