package com.hackathon_5.Yogiyong_In.DTO.Bookmark;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @AllArgsConstructor @Builder
public class BookmarkListResDto {
    private int count;
    private List<BookmarkItemResDto> items;
}
