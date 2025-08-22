package com.hackathon_5.Yogiyong_In.DTO.Bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor @Builder
public class BookmarkListResDto {
    private int count;
    private List<BookmarkItemResDto> items;
}
