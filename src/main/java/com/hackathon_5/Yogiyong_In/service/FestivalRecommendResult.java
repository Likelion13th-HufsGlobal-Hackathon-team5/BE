package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetResDto;

public record FestivalRecommendResult(
        FestivalRecommendGetResDto data,
        String message // null이면 메시지 없댜
) {}
