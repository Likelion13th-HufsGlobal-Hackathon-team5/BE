package com.hackathon_5.Yogiyong_In.dto.AiRecommend;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FestivalRecommendGetItemDto {
    // DB 컬럼명과 1:1로 대응 (변수명은 자바 컨벤션)
    private Integer festivalId;     // festivals.festival_id
    private String  festivalName;   // festivals.festival_name
    private String  festivalDesc;   // festivals.festival_desc
    private String  festivalStart;  // festivals.festival_start (ISO 문자열)
    private String  festivalEnd;    // festivals.festival_end   (ISO 문자열)
    private String  festivalLoca;   // festivals.festival_loca
    private String  imagePath;
    }
