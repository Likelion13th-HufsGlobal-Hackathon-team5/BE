package com.hackathon_5.Yogiyong_In.DTO.Bookmark;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class BookmarkFestivalDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private String festivalStart;
    private String festivalEnd;
    private String festivalLoca;
    private String imagePath;

    public static BookmarkFestivalDto from(Festival f) {
        return BookmarkFestivalDto.builder()
                .festivalId(f.getFestivalId())
                .festivalName(f.getFestivalName())
                .festivalDesc(f.getFestivalDesc())
                .festivalStart(f.getFestivalStart()) // 이제 타입이 일치합니다.
                .festivalEnd(f.getFestivalEnd())     // 이제 타입이 일치합니다.
                .festivalLoca(f.getFestivalLoca())
                .imagePath(f.getImagePath())
                .build();
    }
}