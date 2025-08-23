package com.hackathon_5.Yogiyong_In.dto.Review;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateReqDto {
    private Integer festivalId;
    private String  userId;
    private String reviewTitle;
    private String reviewCont;

}
