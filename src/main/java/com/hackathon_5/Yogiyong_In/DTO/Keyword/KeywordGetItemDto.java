package com.hackathon_5.Yogiyong_In.DTO.Keyword;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "KeywordGetItemDto", description = "키워드 목록의 한 항목 DTO")
public class KeywordGetItemDto {

    @Schema(description = "키워드 ID", example = "1")
    private Integer keywordId;   // ERD: INT → DTO도 Integer로

    @Schema(description = "키워드명", example = "불꽃놀이")
    private String name;         // DB: keyword_name

    @Schema(description = "사용자 선택 여부 (includeSelected=true일 때만 포함)", example = "true", nullable = true)
    private Boolean selected;    // 로그인+includeSelected=true일 때만 값, 아니면 null(응답에서 제외)
}
