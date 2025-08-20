package com.hackathon_5.Yogiyong_In.DTO.Keyword;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordScrollResDto {
    private List<KeywordGetItemDto> items; // [{keywordId, name, iconUrl, selected?}]
    private Integer nextCursor;            // 다음 시작 keywordId (없으면 null)
    private boolean hasNext;
}
