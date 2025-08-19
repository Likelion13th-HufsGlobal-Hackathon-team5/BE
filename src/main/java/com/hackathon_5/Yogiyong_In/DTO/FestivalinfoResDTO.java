package com.hackathon_5.Yogiyong_In.DTO.;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class FestivalInfoResDTO {
    private Long festivalId;
    private String festivalName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String description;
    private String imagePath;
    private String aiReview;
}
