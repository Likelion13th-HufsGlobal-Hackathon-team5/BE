package com.hackathon_5.Yogiyong_In.DTO.Bookmark;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @AllArgsConstructor @Builder
class BookmarkFestivalDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private LocalDate festivalStart;
    private LocalDate festivalEnd;
    private String festivalLoca;
    private String imagePath;

    public static BookmarkFestivalDto from(Festival f) {
        return BookmarkFestivalDto.builder()
                .festivalId(f.getFestivalId())
                .festivalName(f.getFestivalName())
                .festivalDesc(f.getFestivalDesc())
                .festivalStart(f.getFestivalStart())
                .festivalEnd(f.getFestivalEnd())
                .festivalLoca(f.getFestivalLoca())
                .imagePath(f.getImagePath())
                .build();
    }
}
