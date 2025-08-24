package com.hackathon_5.Yogiyong_In.dto.Festival;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PopularFestivalDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate festivalStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate festivalEnd;

    private String festivalLoca;
    private String imagePath;
    private Long bookmarkCount;
}
