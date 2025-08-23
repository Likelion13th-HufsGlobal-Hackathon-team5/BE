package com.hackathon_5.Yogiyong_In.dto.AiReview;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class ReviewSummarizeReqDto {
    private List<String> reviews;
    private Integer maxPoints; // 불릿 개수 제한

}
