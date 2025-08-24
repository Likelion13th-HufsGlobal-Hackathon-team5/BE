package com.hackathon_5.Yogiyong_In.dto.Festival;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalsByKeywordResDto {
    private Integer keywordId;
    private String keywordName;
    private List<FestivalInfoResDto> festivals;
}
