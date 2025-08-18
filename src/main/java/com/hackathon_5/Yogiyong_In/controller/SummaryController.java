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

    @GetMapping("/festivals/{festivalId}")
    public ResponseEntity<ReviewSummarizeResDto> summarizeFestival(
            @PathVariable Integer festivalId,
            @RequestParam(defaultValue = "true") boolean includeQuotes,
            @RequestParam(defaultValue = "5") int topKAspects,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        var res = service.summarizeFestival(festivalId, includeQuotes, topKAspects, forceRefresh);
        return ResponseEntity.ok(res);
    }
}

