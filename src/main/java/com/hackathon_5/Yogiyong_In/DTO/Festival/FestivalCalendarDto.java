package com.hackathon_5.Yogiyong_In.DTO.Festival;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalCalendarDto {

    private Integer festivalId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate festivalStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate festivalEnd;

    // 현재 달 기준 표시 시작일 (ex: 7월 조회 → 7/30)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate displayStart;

    // 현재 달 기준 표시 종료일 (ex: 7월 조회 → 7/31)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate displayEnd;

    // JPQL에서 사용하는 생성자 (festivalId, festivalStart, festivalEnd)
    public FestivalCalendarDto(Integer festivalId, LocalDate festivalStart, LocalDate festivalEnd) {
        this.festivalId = festivalId;
        this.festivalStart = festivalStart;
        this.festivalEnd = festivalEnd;
    }
}
