package com.hackathon_5.Yogiyong_In.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.hackathon_5.Yogiyong_In.DTO.AiRecommend.FestivalRecommendGetItemDto;
import com.hackathon_5.Yogiyong_In.DTO.AiRecommend.FestivalRecommendGetResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalRecommendService {
    private static final String MODEL = "gemini-2.5-flash-lite";
    private static final int DEFAULT_TOP_N = 5;
    private static final int CATALOG_MAX = 120; // 토큰 보호 상한
    private static final int DESC_TRIM = 300;
    private static final int PROMPT_DESC_TRIM = 160;

    private final UserKeywordRepository userKeywordRepository;
    private final FestivalRepository festivalRepository;
    private final Client geminiClient; // GeminiClientConfig 에서 주입

    private final ObjectMapper om = new ObjectMapper();

    @Transactional(readOnly = true)
    public FestivalRecommendResult recommend(String userId, Integer limit) {

        // 1) 사용자 선택 키워드
        var keywordNames = userKeywordRepository.findByUser_UserIdAndIsSelectedTrue(userId).stream()
                .map(uk -> uk.getKeyword().getKeywordName())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (keywordNames.isEmpty()) {
            log.debug("[AI-RECO] userId={} has no selected keywords", userId);
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "선택된 키워드가 없어 추천 결과가 없습니다."
            );
        }

        // 2) 축제 카탈로그
        var all = festivalRepository.findAll();
        if (all.isEmpty()) {
            log.debug("[AI-RECO] No festivals in catalog");
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "등록된 축제 데이터가 없어 추천할 수 없습니다."
            );
        }
        var catalog = all.stream().limit(CATALOG_MAX).toList();

        // 3) 프롬프트
        int topN = (limit == null || limit <= 0) ? DEFAULT_TOP_N : limit;
        String prompt = buildPromptIdsOnly(keywordNames, catalog, topN);

        // 4) Gemini 호출
        String aiText = callGemini(prompt);
        if (aiText.isBlank()) {
            log.warn("[AI-RECO] Empty response from Gemini");
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "AI 응답이 비어 있어 추천 결과가 없습니다."
            );
        }

        // 5) 응답 파싱
        List<Integer> ids = parseFestivalIds(aiText);
        if (ids.isEmpty()) {
            log.warn("[AI-RECO] No ids parsed from Gemini response: {}", aiText);
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "AI 응답을 해석하지 못해 추천 결과가 없습니다."
            );
        }

        // 6) 매핑 → DTO
        Map<Integer, Festival> byId = catalog.stream()
                .collect(Collectors.toMap(Festival::getFestivalId, f -> f));

        var items = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toItemDto)
                .toList();

        var data = FestivalRecommendGetResDto.builder()
                .items(items)
                .totalCount(items.size())
                .build();

        // 결과가 0개라면 안내 메시지
        String msg = items.isEmpty() ? "조건에 맞는 추천 축제가 없습니다." : null;
        return new FestivalRecommendResult(data, msg);
    }

    private FestivalRecommendGetItemDto toItemDto(Festival f) {
        return FestivalRecommendGetItemDto.builder()
                .festivalId(f.getFestivalId())
                .festivalName(f.getFestivalName())
                .festivalDesc(trim(f.getFestivalDesc(), DESC_TRIM))
                .festivalStart(f.getFestivalStart() != null ? f.getFestivalStart().toString() : null)
                .festivalEnd(f.getFestivalEnd() != null ? f.getFestivalEnd().toString() : null)
                .festivalLoca(f.getFestivalLoca())
                .imagePath(f.getImagePath())
                .build();
    }

    private String buildPromptIdsOnly(List<String> kws, List<Festival> catalog, int topN) {
        String kw = String.join(", ", kws);

        StringBuilder sb = new StringBuilder();
        for (Festival f : catalog) {
            sb.append(String.format(
                    "- festival_id: %d | name: %s | loca: %s | start: %s | end: %s | desc: %s%n",
                    f.getFestivalId(),
                    ns(f.getFestivalName()),
                    ns(f.getFestivalLoca()),
                    f.getFestivalStart() != null ? f.getFestivalStart().toString() : "null",
                    f.getFestivalEnd() != null ? f.getFestivalEnd().toString() : "null",
                    trim(ns(f.getFestivalDesc()), PROMPT_DESC_TRIM)
            ));
        }

        return """
               사용자 선택 키워드: [%s]
               아래 '축제 카탈로그'에서 키워드와 가장 잘 맞는 축제의 festival_id를 상위 %d개 고르시오.

               **출력 형식은 숫자만 포함된 JSON 배열**만 허용한다. 예: [12, 7, 3]
               마크다운/설명문/코드펜스 금지. 배열 외 텍스트 출력 금지.

               축제 카탈로그:
               %s
               """.formatted(kw, topN, sb);
    }

    private String callGemini(String prompt) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .candidateCount(1)
                .maxOutputTokens(200) // JSON 배열만 필요하므로 작게
                .build();

        GenerateContentResponse resp = geminiClient.models.generateContent(
                MODEL,
                prompt,
                config
        );

        String result = resp.text();
        return (result == null) ? "" : result.trim();
    }

    private List<Integer> parseFestivalIds(String aiText) {
        if (aiText == null || aiText.isBlank()) return List.of();

        Pattern p = Pattern.compile("\\[\\s*(?:\\d+\\s*(?:,\\s*\\d+\\s*)*)?\\]");
        Matcher m = p.matcher(aiText);
        String json = m.find() ? m.group() : aiText;

        try {
            return om.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception primary) {
            try {
                record IdObj(Integer festival_id) {}
                List<IdObj> objs = om.readValue(json, new TypeReference<List<IdObj>>() {});
                return objs.stream().map(IdObj::festival_id).filter(Objects::nonNull).toList();
            } catch (Exception secondary) {
                log.warn("Gemini 응답 파싱 실패: {}", secondary.getMessage());
                return List.of();
            }
        }
    }

    private static String ns(String s){ return s == null ? "" : s; }
    private static String trim(String s, int max){
        if (s == null) return null;
        String t = s.replaceAll("\\s+"," ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
