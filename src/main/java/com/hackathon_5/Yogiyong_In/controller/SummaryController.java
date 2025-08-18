package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.AiReview.ReviewSummarizeReqDto;
import com.hackathon_5.Yogiyong_In.DTO.AiReview.ReviewSummarizeResDto;
import com.hackathon_5.Yogiyong_In.service.ReviewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final ReviewSummaryService service;

    @PostMapping
    public ResponseEntity<ReviewSummarizeResDto> summarize(@RequestBody ReviewSummarizeReqDto req) {
        String result = service.summarize(req.getReviews(), req.getMaxPoints());
        return ResponseEntity.ok(
                new ReviewSummarizeResDto(result, "gemini-2.5-flash-lite")
        );
    }
}

