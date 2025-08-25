package com.hackathon_5.Yogiyong_In.dto.AiRecommend;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FestivalRecommendGetResDto {
    private String title; //★ 제목필드 추가
    private List<FestivalRecommendGetItemDto> items; // 추천 축제 목록
    private int totalCount;                           // 총 개수
}


