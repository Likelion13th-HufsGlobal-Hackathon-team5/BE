package com.hackathon_5.Yogiyong_In.DTO.Review;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateReqDto {
    private Integer reviewId;
    private String  userId;
    private String reviewTitle;
    private String rewviewCont;

}
