package com.hackathon_5.Yogiyong_In.service;

import com.google.genai.Client;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.ServerException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.dto.AiReview.ReviewSummarizeResDto;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalAiReviewService {

    private final Client genaiClient;
    private final FestivalRepository festivalRepository;
    private final ReviewRepository reviewRepository;

    private static final String MODEL = "gemini-2.5-flash-lite";
    private static final String DEFAULT_DESC = "설명 없음";

    // FestivalAiReviewService.java 안의 summarizeFestival(...)만 교체
    public ReviewSummarizeResDto summarizeFestival(
            Integer festivalId,
            boolean includeQuotes,
            int topKAspects,
            boolean forceRefresh
    ) {
        if (forceRefresh) {
            log.debug("forceRefresh requested for festivalId={}", festivalId);
        }

        Festival f = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        List<String> reviews = reviewRepository.findTextsByFestivalId(festivalId);

        // ✅ 최소 1개면 생성(0개만 막음)
        final int MAX_REVIEWS = 60;
        final int MAX_CHARS_PER = 400;

        if (reviews == null || reviews.isEmpty()) {
            return ReviewSummarizeResDto.builder()
                    .summary("아직 리뷰가 없어 요약을 생성하지 않았습니다.")
                    .model(MODEL)
                    .build();
        }

        // 토큰 초과 방지: 최신순 일부 + 너무 긴 리뷰는 자르기
        StringBuilder sb = new StringBuilder();
        int use = Math.min(reviews.size(), MAX_REVIEWS);
        for (int i = 0; i < use; i++) {
            String r = reviews.get(i);
            if (r == null) continue;
            String clipped = r.length() > MAX_CHARS_PER ? r.substring(0, MAX_CHARS_PER) + "..." : r;
            sb.append("- ").append(clipped.replace("\n", " ")).append("\n");
        }
        String reviewsBlock = sb.toString();

        // 리뷰가 1개면 인용도 1개로 표현하도록 안내 문구만 살짝 다르게
        String quotesLine = includeQuotes
                ? (reviews.size() == 1 ? "대표 인용 1개 (있을 때만, \"...\")" : "대표 인용 1~2개 (있을 때만, \"...\")")
                : "인용은 포함하지 않음";

        Content system = Content.fromParts(Part.fromText(
                String.format("""
            너는 한국어로 '축제 리뷰 요약'을 작성한다. 지침:
            - 핵심 포인트 %d개 이내의 불릿으로 요약
            - 긍/부정 균형 잡기, 과장 금지
            - 중복/자잘한 내용은 묶어서 표현
            - 숫자/가격/날짜는 리뷰에 명확히 있지 않으면 추측하지 말 것
            - 출력 형식:
              1) 한 줄 총평
              2) 불릿 핵심 포인트 (%d개 이내)
              3) %s
            """,
                        Math.max(1, topKAspects),
                        Math.max(1, topKAspects),
                        quotesLine
                )
        ));

        String prompt = String.format("""
            [축제 정보]
            - 이름: %s
            - 설명: %s

            [사용자 리뷰 모음 (최신순 일부)]
            %s

            위 지침에 따라 간단하고 읽기 쉬운 요약을 작성해줘.
            """,
                f.getFestivalName(),
                nvl(f.getFestivalDesc()),
                reviewsBlock
        );

        GenerateContentConfig config = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(600)
                .systemInstruction(system)
                .safetySettings(
                        SafetySetting.builder()
                                .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                                .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                                .build()
                )
                .build();

        try {
            GenerateContentResponse resp = genaiClient.models.generateContent(MODEL, prompt, config);
            String text = resp.text();
            String summary = (text == null || text.isBlank()) ? "요약을 생성하지 못했습니다." : text.trim();

            return ReviewSummarizeResDto.builder()
                    .summary(summary)
                    .model(MODEL)
                    .build();

        } catch (ClientException e) {
            String userMsg = userMsgFromClientException(e);
            log.warn("GenAI ClientException: {}", e.getMessage(), e);
            return ReviewSummarizeResDto.builder().summary(userMsg).model(MODEL).build();

        } catch (ServerException e) {
            log.error("GenAI ServerException: {}", e.getMessage(), e);
            return ReviewSummarizeResDto.builder()
                    .summary("AI 서비스 장애로 요약을 생성하지 못했습니다.")
                    .model(MODEL)
                    .build();

        } catch (ApiException e) {
            log.error("GenAI ApiException: {}", e.getMessage(), e);
            return ReviewSummarizeResDto.builder()
                    .summary("AI 요약 생성 중 오류가 발생했습니다.")
                    .model(MODEL)
                    .build();

        } catch (Exception e) {
            log.error("GenAI 호출 중 알 수 없는 예외", e);
            return ReviewSummarizeResDto.builder()
                    .summary("AI 요약 생성 중 알 수 없는 오류가 발생했습니다.")
                    .model(MODEL)
                    .build();
        }
    }


    // 기존 간단 리뷰
    public String generateFestivalReview(String festivalDesc) {
        if (festivalDesc == null || festivalDesc.isBlank()) {
            return "축제 설명이 없어 리뷰를 생성할 수 없습니다.";
        }

        Content system = Content.fromParts(Part.fromText("""
                너는 한국어 '축제 소개 리뷰' 작성가다. 지침:
                - 총 2~3문장, 과장 없이 담백하게
                - 핵심 포인트 2개 이내
                - 날짜/가격 등은 추측 금지
                - 출력은 한국어 평서문
                - 제목, 번호, 불릿포인트, 마크다운 문법 사용 금지
                - 오직 줄글로만 작성
                - 문체는 ~합니다체로 작성
                - 축제 이름은 사용하지 말 것
                """));

        String prompt = String.format("""
                아래는 축제 설명이다.
                ---
                %s
                ---
                위 지침에 따라 간단 리뷰를 작성해줘.
                """, festivalDesc.trim());

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

        try {
            GenerateContentResponse resp = genaiClient.models.generateContent(MODEL, prompt, config);
            String text = resp.text();
            return (text == null || text.isBlank())
                    ? "요청한 리뷰를 생성하지 못했습니다."
                    : text.trim();
        } catch (Exception e) {
            log.error("GenAI 호출 실패", e);
            return "AI 리뷰 생성에 실패했습니다.";
        }
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? DEFAULT_DESC : s;
    }

    private static String userMsgFromClientException(ClientException e) {
        String m = (e.getMessage() == null) ? "" : e.getMessage().toLowerCase();
        if (m.contains("api key not valid") || m.contains("invalid api key")) {
            return "AI 구성 오류로 요약을 생성하지 못했습니다. (API 키를 확인하세요)";
        } else if (m.contains("quota") || m.contains("exceed") || m.contains("rate")) {
            return "AI 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";
        }
        return "AI 요약 생성 중 클라이언트 오류가 발생했습니다.";
    }
}
