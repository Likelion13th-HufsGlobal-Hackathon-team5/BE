package com.hackathon_5.Yogiyong_In.DTO.Review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "ReviewGetUserResDto", description = "리뷰 작성자 정보 DTO")
public class ReviewGetUserResDto {

    @Schema(description = "작성자 ID (DB VARCHAR(20))", example = "user_12345")
    private String userId;    // DB는 VARCHAR(20)

    @Schema(description = "작성자 닉네임", example = "요기용")
    private String nickname;
}
