package com.hackathon_5.Yogiyong_In.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetItemDto;
import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

        // 1) 사용자 선택 키워드(표시명 + slug)
        var selected = userKeywordRepository.findByUser_UserIdAndIsSelectedTrue(userId);
        var keywordNames = selected.stream() // 프롬프트 표기용
                .map(uk -> uk.getKeyword().getKeywordName())
                .filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isBlank()).distinct().toList();

        var keywordSlugs = selected.stream() // 내부 로직용(한글 slug)
                .map(uk -> uk.getKeyword().getSlug())
                .filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isBlank()).distinct().toList();

        if (keywordNames.isEmpty()) {
            log.debug("[AI-RECO] userId={} has no selected keywords", userId);
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "선택된 키워드가 없어 추천 결과가 없습니다."
            );
        }

        // 2) 축제 카탈로그 로딩
        var all = festivalRepository.findAll();
        if (all.isEmpty()) {
            log.debug("[AI-RECO] No festivals in catalog");
            return new FestivalRecommendResult(
                    FestivalRecommendGetResDto.builder().items(List.of()).totalCount(0).build(),
                    "등록된 축제 데이터가 없어 추천할 수 없습니다."
            );
        }

        // 2-1) 계절 우선 선별/정렬
        var seasonMonths = seasonMonthsBySlugs(keywordSlugs); // 예) [3,4,5] 등
        List<Festival> catalog = all;
        if (!seasonMonths.isEmpty()) {
            // 계절 점수 + 보조 점수(이름/설명에 계절 단어가 있으면 소폭 가점)
            catalog = all.stream()
                    .sorted(Comparator.comparingInt((Festival f) -> seasonScore(f, seasonMonths))
                            .thenComparingInt(f -> textSeasonBias(f, seasonMonths))
                            .reversed())
                    .limit(CATALOG_MAX)
                    .toList();
        } else {
            catalog = all.stream().limit(CATALOG_MAX).toList();
        }

        // 3) 프롬프트
        int topN = (limit == null || limit <= 0) ? DEFAULT_TOP_N : limit;
        String prompt = buildPromptIdsOnly(keywordNames, seasonMonths, catalog, topN);

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
                .build();
    }

    private String buildPromptIdsOnly(List<String> kws, Set<Integer> seasonMonths, List<Festival> catalog, int topN) {
        String kw = String.join(", ", kws);

        String seasonHint = seasonMonths.isEmpty()
                ? ""
                : """
                  - 계절 키워드가 선택되었으므로, 축제 시작/종료 월이 해당 월( %s )에 포함되는 축제를 **강하게 우선**하세요.
                  """.formatted(seasonMonths.stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")));

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
               아래 '축제 카탈로그'에서 **키워드와 가장 잘 맞는 축제의 festival_id**를 상위 %d개 고르시오.
               %s
               **출력 형식은 숫자만 포함된 JSON 배열**만 허용한다. 예: [12, 7, 3]
               마크다운/설명문/코드펜스 금지. 배열 외 텍스트 출력 금지.

               축제 카탈로그:
               %s
               """.formatted(kw, topN, seasonHint, sb);
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

    // ---------- 계절 매핑/스코어링 유틸 ----------

    /** 선택한 slug(한글) 목록에서 계절 월 집합을 만든다. */
    private Set<Integer> seasonMonthsBySlugs(List<String> slugs) {
        Set<Integer> ms = new HashSet<>();
        if (slugs == null) return ms;
        if (slugs.contains("봄"))   ms.addAll(List.of(3,4,5));
        if (slugs.contains("여름")) ms.addAll(List.of(6,7,8));
        if (slugs.contains("가을")) ms.addAll(List.of(9,10,11));
        if (slugs.contains("겨울")) ms.addAll(List.of(12,1,2));
        return ms;
    }

    /** 축제가 선택 월과 겹치면 높은 점수를 부여(겹치는 정도에 따라 가중) */
    private int seasonScore(Festival f, Set<Integer> months) {
        if (months == null || months.isEmpty()) return 0;
        LocalDate s = f.getFestivalStart();
        LocalDate e = f.getFestivalEnd();
        if (s == null && e == null) return 0;

        // 시작/종료 월만 있는 단순 케이스 가중
        int score = 0;
        if (s != null && months.contains(s.getMonthValue())) score += 3;
        if (e != null && months.contains(e.getMonthValue())) score += 2;

        // 기간이 길면 월 겹침을 추가 가점(간단 루프)
        if (s != null && e != null) {
            Set<Integer> span = enumerateMonthsInclusive(s, e);
            long overlap = span.stream().filter(months::contains).count();
            score += overlap; // 겹치는 월 수만큼 +1
        }
        return score;
    }

    /** 이름/설명에 계절 단어가 등장하면 소폭 가점(보조용) */
    private int textSeasonBias(Festival f, Set<Integer> months) {
        if (months == null || months.isEmpty()) return 0;
        String text = (ns(f.getFestivalName()) + " " + ns(f.getFestivalDesc())).toLowerCase();
        int b = 0;
        if (months.contains(3) || months.contains(4) || months.contains(5))
            if (text.contains("봄") || text.contains("벚꽃")) b += 1;
        if (months.contains(6) || months.contains(7) || months.contains(8))
            if (text.contains("여름") || text.contains("피서") || text.contains("물놀이")) b += 1;
        if (months.contains(9) || months.contains(10) || months.contains(11))
            if (text.contains("가을") || text.contains("단풍")) b += 1;
        if (months.contains(12) || months.contains(1) || months.contains(2))
            if (text.contains("겨울") || text.contains("눈") || text.contains("스노우")) b += 1;
        return b;
    }

    /** 시작~종료 기간의 월을 모두 뽑는다(연도 경계 포함) */
    private Set<Integer> enumerateMonthsInclusive(LocalDate start, LocalDate end) {
        Set<Integer> set = new HashSet<>();
        if (start == null || end == null) return set;
        LocalDate cur = start;
        int guard = 0; // 무한루프 방지
        while (!cur.isAfter(end) && guard++ < 370) {
            set.add(cur.getMonthValue());
            cur = cur.plusMonths(1).withDayOfMonth(1);
        }
        return set;
        // 참고: start > end인 비정상 데이터는 상단 seasonScore에서 처리하지 않음
    }

    // ---------- 공통 유틸 ----------

    private static String ns(String s){ return s == null ? "" : s; }
    private static String trim(String s, int max){
        if (s == null) return null;
        String t = s.replaceAll("\\s+"," ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
