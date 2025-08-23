package com.hackathon_5.Yogiyong_In.dto.Bookmark;

import lombok.*;

@Getter @AllArgsConstructor @Builder
public class BookmarkCreateResDto {
    private boolean created;
    private Long bookmarkId;
}