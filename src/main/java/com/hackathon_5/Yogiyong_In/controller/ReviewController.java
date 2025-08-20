package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.Review.ReviewCreateReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Review.ReviewCreateResDto;
import com.hackathon_5.Yogiyong_In.DTO.Review.ReviewScrollResDto;
import com.hackathon_5.Yogiyong_In.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "리뷰 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "축제 리뷰 목록(스크롤)",
            description = "festivalId에 대한 리뷰를 cursor(마지막 reviewId) 이후부터 size만큼 반환.")
    @GetMapping("/festivals/{festivalId}/reviews")
    public ResponseEntity<ReviewScrollResDto> getFestivalReviews(
            @PathVariable Integer festivalId,
            @Parameter(description = "이 커서(마지막 reviewId) 이후부터 조회") @RequestParam(required = false) Integer cursor,
            @Parameter(description = "가져올 개수(기본 20, 최대 100)") @RequestParam(defaultValue = "20") Integer size
    ) {
        int s = (size == null || size <= 0) ? 20 : Math.min(size, 100);
        return ResponseEntity.ok(
                reviewService.getReviewsScroll(festivalId, cursor, s)
        );
    }
    @Operation(summary = "리뷰 작성", description = "리뷰 제목, 내용, 유저 ID, 축제 ID를 받아 리뷰를 작성합니다.")
    @PostMapping("/reviews")
    public ResponseEntity<ReviewCreateResDto> createReview(
            @RequestBody ReviewCreateReqDto reqDto
    ) {
        return ResponseEntity.ok(reviewService.createReview(reqDto));
    }
}
