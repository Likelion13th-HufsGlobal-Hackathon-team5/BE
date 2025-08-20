package com.hackathon_5.Yogiyong_In.DTO.Review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ReviewCreateResDto {
    private Integer reviewId;
    private String reviewTitle;
    private String reviewCont;
    private String nickname;
    private LocalDateTime createdAt;
}
