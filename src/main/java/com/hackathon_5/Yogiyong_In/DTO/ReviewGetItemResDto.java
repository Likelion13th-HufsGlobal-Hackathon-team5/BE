package com.hackathon_5.Yogiyong_In.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ReviewGetItemResDto", description = "리뷰 단건 응답 DTO")
public class ReviewGetItemResDto {

    @Schema(description = "리뷰 ID", example = "101")
    private Integer reviewId;

    @Schema(description = "작성자 정보", implementation = ReviewGetUserResDto.class)
    private ReviewGetUserResDto user;

    @Schema(description = "리뷰 제목", example = "불꽃놀이 최고의 자리 찾는 법")
    private String title;       // DB: review_title

    @Schema(description = "리뷰 본문 내용", example = "여의도 한강공원 중앙 무대 근처가 최고였어요!")
    private String content;     // DB: review_cont

    @Schema(description = "작성일시 (ISO 8601)", example = "2025-07-31T12:30:15Z")
    private String createdAt;   // DB: created_at → 문자열(ISO)로 직렬화
}
