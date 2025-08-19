package com.hackathon_5.Yogiyong_In.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FestivalInfoReqDTO {
    private String festivalName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;
    private String imagePath;
    private String aiReview;
}
