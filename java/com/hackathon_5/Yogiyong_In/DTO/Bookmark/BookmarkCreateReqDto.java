package com.hackathon_5.Yogiyong_In.DTO.Bookmark;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookmarkCreateReqDto {
    private Integer festivalId; // 저장할 축제 ID
}