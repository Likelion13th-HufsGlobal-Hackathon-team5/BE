package com.hackathon_5.Yogiyong_In.dto.Bookmark;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import lombok.*;

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
                .festivalStart(f.getFestivalStart() != null ? f.getFestivalStart().toString() : null) // ✅ null-safe
                .festivalEnd(f.getFestivalEnd() != null ? f.getFestivalEnd().toString() : null)       // ✅ null-safe
                .festivalLoca(f.getFestivalLoca())
                .imagePath(f.getImagePath())
                .build();
    }

}