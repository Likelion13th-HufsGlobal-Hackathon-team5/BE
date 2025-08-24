package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.dto.AiReview.ReviewSummarizeResDto;
import com.hackathon_5.Yogiyong_In.service.FestivalAiReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI Review Summary", description = "축제 리뷰를 LLM으로 요약")
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class AireviewController {

    private final FestivalAiReviewService festivalAiReviewService;

    @Operation(summary = "AI 리뷰 요약",
            description = "축제의 사용자 리뷰를 취합해 요약을 반환합니다. " +
                    "includeQuotes(기본 true), topKAspects(기본 6), forceRefresh(기본 false)")
    @GetMapping(value = "/festivals/{festivalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewSummarizeResDto> summarizeFestival(
            @PathVariable Integer festivalId,
            @RequestParam(name = "includeQuotes", required = false) String includeQuotes,
            @RequestParam(name = "topKAspects", required = false) String topKAspects,
            @RequestParam(name = "forceRefresh", required = false) String forceRefresh
    ) {
        boolean include = parseBooleanOrDefault(includeQuotes, true);
        int topK = clamp(parseIntOrDefault(topKAspects, 6), 1, 10); // 1~10 사이로 권장 클램프
        boolean refresh = parseBooleanOrDefault(forceRefresh, false);

        var res = festivalAiReviewService.summarizeFestival(festivalId, include, topK, refresh);
        return ResponseEntity.ok(res);
    }

    private static boolean parseBooleanOrDefault(String v, boolean def) {
        if (v == null || v.isBlank()) return def;
        String s = v.trim().toLowerCase();
        return s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y");
    }

    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isBlank()) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    // 존재하지 않는 축제 등 IllegalArgumentException → 404로 매핑
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
