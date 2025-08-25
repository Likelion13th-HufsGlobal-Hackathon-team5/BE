package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.ApiResponse;
import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetResDto;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalsByKeywordResDto;
import com.hackathon_5.Yogiyong_In.service.FestivalRecommendService;
import com.hackathon_5.Yogiyong_In.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Festival", description = "축제 정보 및 추천 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festivals")
public class FestivalRecommendController {

    private final FestivalRecommendService recommendService;
    private final FestivalService festivalService;

    //  [수정] recommend 메소드를 아래 내용으로 교체했습니다.
    @Operation(
            summary = "AI 축제 추천 (키워드별 그룹)",
            description = "로그인한 사용자의 선택 키워드별로 AI가 추천하는 축제 목록을 그룹화하여 반환합니다.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recommend")
    public ApiResponse<List<FestivalRecommendGetResDto>> recommend(
            Authentication authentication,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit
    ) {
        String userId = authentication.getName();
        List<FestivalRecommendGetResDto> result = recommendService.recommend(userId, limit);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "키워드로 축제 검색 (DB 기반, 그룹화)",
            description = "선택된 키워드 ID 목록을 받아, 각 키워드별로 해당하는 축제 목록을 그룹으로 묶어 반환합니다."
    )
    @Parameter(name = "keywordIds", description = "쉼표로 구분된 키워드 ID 목록 (예: 1,5,12)", required = true)
    @GetMapping("/by-keywords")
    public ResponseEntity<List<FestivalsByKeywordResDto>> getFestivalsByKeywords(
            @RequestParam List<Integer> keywordIds
    ) {
        List<FestivalsByKeywordResDto> result = festivalService.findFestivalsGroupedByKeyword(keywordIds);
        return ResponseEntity.ok(result);
    }
}
