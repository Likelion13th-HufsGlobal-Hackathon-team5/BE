package com.hackathon_5.Yogiyong_In.DTO.Festival;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalInfoResDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private LocalDate festivalStart;  // ← String → LocalDate
    private LocalDate festivalEnd;    // ← String → LocalDate
    private String festivalLoca;
    private String imagePath;
    private String aiReview;
}
