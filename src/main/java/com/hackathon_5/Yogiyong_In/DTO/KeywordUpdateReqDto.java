package com.hackathon_5.Yogiyong_In.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "KeywordUpdateReqDto", description = "선택할 키워드 ID 목록 요청 DTO (PUT Body)")
public class KeywordUpdateReqDto {

    @Schema(
            description = "선택할 키워드 ID 배열",
            example = "[1, 2, 5]"
    )
    private List<Integer> keywordIds;
}
