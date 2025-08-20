package com.hackathon_5.Yogiyong_In.DTO;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FestivalInfoResDTO {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private LocalDate festivalStart;
    private LocalDate festivalEnd;
    private String festivalLoca;
    private String imagePath;
    private String aiReview;
}
