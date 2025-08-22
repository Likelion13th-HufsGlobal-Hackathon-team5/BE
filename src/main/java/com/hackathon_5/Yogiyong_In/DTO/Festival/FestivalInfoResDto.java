package com.hackathon_5.Yogiyong_In.DTO.Festival;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalInfoResDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate festivalStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate festivalEnd;

    private String festivalLoca;
    private String imagePath;
    private String aiReview;

    public static FestivalInfoResDto fromEntity(Festival f, String aiReview) {
        return FestivalInfoResDto.builder()
                .festivalId(f.getFestivalId())
                .festivalName(f.getFestivalName())
                .festivalDesc(f.getFestivalDesc())
                .festivalStart(f.getFestivalStart())
                .festivalEnd(f.getFestivalEnd())
                .festivalLoca(f.getFestivalLoca())
                .imagePath(f.getImagePath())
                .aiReview(aiReview)
                .build();
    }


    private static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) return null;
        // DB가 varchar(예: 2025-10-03, 2025/10/03, 2025.10.03 등)일 수 있어 보수적으로 처리
        String normalized = value.trim()
                .replace('.', '-')
                .replace('/', '-');
        // yyyy-MM-dd 앞 10자리만 안전하게 파싱
        if (normalized.length() >= 10) normalized = normalized.substring(0, 10);
        return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
