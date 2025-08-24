package com.hackathon_5.Yogiyong_In.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AuthLoginResDto {
    private String tokenType;
    private String accessToken;
    private long   expiresIn;
    private String userId;
    private String nickname;
    private Boolean hasAiKeywordHistory;
}
