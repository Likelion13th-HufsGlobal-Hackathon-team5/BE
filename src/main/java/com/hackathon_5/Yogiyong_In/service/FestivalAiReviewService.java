package com.hackathon_5.Yogiyong_In.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.hackathon_5.Yogiyong_In.dto.AiReview.ReviewSummarizeResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalAiReviewService {

    private final Client client;
    private final FestivalRepository festivalRepository;

    private static final String MODEL = "gemini-2.5-flash-lite";

    // NEW: 요약 엔드포인트에서 사용하는 메서드
    public ReviewSummarizeResDto summarizeFestival(
            Integer festivalId,
            boolean includeQuotes,
            int topKAspects,
            boolean forceRefresh
    ) {
        Festival f = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        // 우선은 기존 리뷰 생성기를 재사용하여 간단 요약을 반환
        String summary = generateFestivalReview(f.getFestivalDesc());

        return ReviewSummarizeResDto.builder()
                .summary(summary)   // 생성한 요약
                .model(MODEL)       // "gemini-2.5-flash-lite"
                .build();
    }

    // 이미 존재하던 메서드 (변경 없음)
    public String generateFestivalReview(String festivalDesc) {
        if (festivalDesc == null || festivalDesc.isBlank()) {
            return "축제 설명이 없어 리뷰를 생성할 수 없습니다.";
        }

        Content system = Content.fromParts(Part.fromText(
                """
                너는 한국어 '축제 소개 리뷰' 작성가야. 아래 지침을 따른다:
                - 총 2~3문장, 과장 없이 담백하게
                - 주요 포인트 2개 이내로 요약 (예: 분위기, 프로그램, 가족/연인 추천)
                - 특정 날짜/가격 등 확정 정보는 추측하지 말 것
                - 출력은 한국어 평서문
                """
        ));

        String prompt = """
                아래는 축제 설명이다.
                ---
                %s
                ---
                위 지침에 따라 간단 리뷰를 작성해줘.
                """.formatted(festivalDesc.trim());

        GenerateContentConfig config = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(300)
                .systemInstruction(system)
                .safetySettings(
                        SafetySetting.builder()
                                .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                                .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                                .build()
                )
                .build();

        GenerateContentResponse resp = client.models.generateContent(MODEL, prompt, config);
        String text = resp.text();
        return (text == null || text.isBlank())
                ? "요청한 리뷰를 생성하지 못했습니다."
                : text.trim();
    }
}
