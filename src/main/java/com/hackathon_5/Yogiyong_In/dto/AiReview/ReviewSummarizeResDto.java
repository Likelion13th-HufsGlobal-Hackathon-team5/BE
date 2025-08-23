package com.hackathon_5.Yogiyong_In.dto.AiReview;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewSummarizeResDto {
    private String summary;
    private String model;

}
