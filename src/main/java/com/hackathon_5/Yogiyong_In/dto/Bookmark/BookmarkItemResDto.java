package com.hackathon_5.Yogiyong_In.dto.Bookmark;

import lombok.*;

@Getter @AllArgsConstructor @Builder
public class BookmarkItemResDto {
    private Long bookmarkId;
    private BookmarkFestivalDto festival;
}