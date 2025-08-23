package com.hackathon_5.Yogiyong_In.dto.Bookmark;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkCreateReqDto {
    private Integer festivalId; // 저장할 축제 ID
}