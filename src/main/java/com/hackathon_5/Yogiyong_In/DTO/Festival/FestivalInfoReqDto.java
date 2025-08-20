package com.hackathon_5.Yogiyong_In.DTO.Festival;


import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FestivalInfoReqDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private String festivalStart;
    private String festivalEnd;
    private String festivalLoca;
    private String imagePath;
    private String aiReview;
}

