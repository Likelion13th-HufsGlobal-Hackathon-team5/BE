package com.hackathon_5.Yogiyong_In.service;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.hackathon_5.Yogiyong_In.DTO.AiReview.ReviewSummarizeResDto;
import com.hackathon_5.Yogiyong_In.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewSummaryService {

    private final Client client;
    private final ReviewRepository reviewRepository;
    private static final String MODEL = "gemini-2.5-flash-lite";

    public ReviewSummaryService(Client client, ReviewRepository reviewRepository) {
        this.client = client;
        this.reviewRepository = reviewRepository;
    }

    public ReviewSummarizeResDto summarizeFestival(Integer festivalId,
                                                   boolean includeQuotes,
                                                   int topKAspects,
                                                   boolean forceRefresh) {
        List<String> texts = reviewRepository.findTextsByFestivalId(festivalId);
        if (texts == null || texts.isEmpty()) {
            return new ReviewSummarizeResDto("요약할 리뷰가 없습니다.", MODEL);
        }

        String summary = summarize(texts, 6);
        return new ReviewSummarizeResDto(summary, MODEL);
    }

    public String summarize(List<String> reviews, Integer maxPoints) {
        if (reviews == null || reviews.isEmpty()) {
            return "요약할 리뷰가 없습니다.";
        }
        String joined = joinWithLimit(reviews, 15000);

        Content system = Content.fromParts(Part.fromText(
                """
                너는 한국어 축제 리뷰 요약가야. 아래 요구사항을 따른다:
                - 불릿 형태로 핵심 요점만 요약 (중복 제거)
                - '전반 감성(긍정/부정/중립) 비율', '주요 장점(칭찬)', '주요 단점(불만)', '개선 제안' 섹션 포함
                - 숫자/빈도는 과장 없이 보수적으로 표현
                - 출력은 한국어로, 마크다운 불릿 사용
                """
        ));

        String prompt = """
            다음은 사용자 리뷰 모음이야.
            리뷰:
            ---
            %s
            ---
            위 지침에 맞춰 최대 %d개 불릿으로 핵심만 요약해줘.
            """.formatted(joined, (maxPoints == null ? 7 : Math.max(3, maxPoints)));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(800)
                .systemInstruction(system)
                .safetySettings(
                        SafetySetting.builder()
                                .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                                .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                                .build()
                )
                .build();

        GenerateContentResponse resp = client.models.generateContent(MODEL, prompt, config);
        String result = resp.text();
        if (result == null || result.isBlank()) {
            return "요약 결과가 비어 있습니다.";
        }
        return result;
    }

    private String joinWithLimit(List<String> items, int charLimit) {
        String joined = items.stream()
                .map(s -> "- " + s.replaceAll("\\s+", " ").trim())
                .collect(Collectors.joining("\n"));
        if (joined.length() <= charLimit) return joined;
        return joined.substring(0, charLimit) + "\n...(이하 생략)";
    }
}