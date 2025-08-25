//1) DTO id 전용_북마크에서 다른 정보말고 축제 아이디만
// src/main/java/com/hackathon_5/Yogiyong_In/dto/Bookmark/BookmarkIdListResDto.java
package com.hackathon_5.Yogiyong_In.dto.Bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor @Builder
public class BookmarkIdListResDto {
    private int count;
    private List<Integer> festivalIds;
}