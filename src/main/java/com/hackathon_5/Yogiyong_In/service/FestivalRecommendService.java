package com.hackathon_5.Yogiyong_In.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.UserKeyword;
import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetItemDto;
import com.hackathon_5.Yogiyong_In.dto.AiRecommend.FestivalRecommendGetResDto;
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

    // ✅ [수정] recommend 메소드를 아래 내용으로 완전히 교체합니다.
    @Transactional(readOnly = true)
    public List<FestivalRecommendGetResDto> recommend(String userId, Integer limit) {
        // 1) 사용자 선택 키워드(엔티티) 목록 가져오기
        List<UserKeyword> selectedUserKeywords = userKeywordRepository.findByUser_UserIdAndIsSelectedTrue(userId);

        if (selectedUserKeywords.isEmpty()) {
            log.debug("[AI-RECO] userId={} has no selected keywords", userId);
            return new ArrayList<>(); // 키워드가 없으면 빈 리스트 반환
        }

        // 2) 전체 축제 카탈로그 한 번만 로딩해서 Map으로 변환 (조회 성능 향상)
        Map<Integer, Festival> festivalMap = festivalRepository.findAll().stream()
                .collect(Collectors.toMap(Festival::getFestivalId, f -> f));

        if (festivalMap.isEmpty()) {
            log.debug("[AI-RECO] No festivals in catalog");
            return new ArrayList<>();
        }
        List<Festival> allFestivals = new ArrayList<>(festivalMap.values());

        // 3) 최종 결과를 담을 리스트 생성 (반환 타입이 List<FestivalRecommendGetResDto>로 변경)
        List<FestivalRecommendGetResDto> finalResult = new ArrayList<>();

        // 4) ★ 각 키워드별로 루프를 돌며 AI 추천 요청
        for (UserKeyword userKeyword : selectedUserKeywords) {
            String keywordName = userKeyword.getKeyword().getKeywordName();
            String keywordSlug = userKeyword.getKeyword().getSlug();

            // 4-1) 현재 키워드로 제목 생성
            String title = "'" + keywordName + "' 축제";

            // 4-2) 현재 키워드 하나만으로 AI 프롬프트 생성
            String prompt = buildPromptForKeyword(keywordName, keywordSlug, allFestivals, limit);

            // 4-3) AI 호출 및 ID 파싱
            String aiText = callGemini(prompt);
            List<Integer> ids = parseFestivalIds(aiText);

            if (ids.isEmpty()) {
                continue; // 현재 키워드에 대한 추천 결과가 없으면 다음 키워드로 넘어감
            }

            // 4-4) ID를 축제 정보(DTO)로 변환
            var items = ids.stream()
                    .map(festivalMap::get)
                    .filter(Objects::nonNull)
                    .map(this::toItemDto)
                    .toList();

            // 4-5) 현재 키워드 그룹을 FestivalRecommendGetResDto에 담아 최종 리스트에 추가
            if (!items.isEmpty()) {
                finalResult.add(
                        FestivalRecommendGetResDto.builder()
                                .title(title)
                                .items(items)
                                .totalCount(items.size())
                                .build()
                );
            }
        }

        return finalResult;
    }

    // ✅ [추가] 루프 안에서 프롬프트를 만드는 로직을 별도 메소드로 분리
    private String buildPromptForKeyword(String keywordName, String keywordSlug, List<Festival> allFestivals, Integer limit) {
        var seasonMonths = seasonMonthsBySlugs(List.of(keywordSlug));
        List<Festival> catalogForPrompt = allFestivals.stream().limit(CATALOG_MAX).toList();
        int topN = (limit == null || limit <= 0) ? DEFAULT_TOP_N : limit;
        return buildPromptIdsOnly(List.of(keywordName), seasonMonths, catalogForPrompt, topN);
    }

    // --- 이하 다른 메소드들은 그대로 유지 ---

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

    private Set<Integer> seasonMonthsBySlugs(List<String> slugs) {
        Set<Integer> ms = new HashSet<>();
        if (slugs == null) return ms;
        if (slugs.contains("봄"))   ms.addAll(List.of(3,4,5));
        if (slugs.contains("여름")) ms.addAll(List.of(6,7,8));
        if (slugs.contains("가을")) ms.addAll(List.of(9,10,11));
        if (slugs.contains("겨울")) ms.addAll(List.of(12,1,2));
        return ms;
    }

    private int seasonScore(Festival f, Set<Integer> months) {
        if (months == null || months.isEmpty()) return 0;
        LocalDate s = f.getFestivalStart();
        LocalDate e = f.getFestivalEnd();
        if (s == null && e == null) return 0;

        int score = 0;
        if (s != null && months.contains(s.getMonthValue())) score += 3;
        if (e != null && months.contains(e.getMonthValue())) score += 2;

        if (s != null && e != null) {
            Set<Integer> span = enumerateMonthsInclusive(s, e);
            long overlap = span.stream().filter(months::contains).count();
            score += overlap;
        }
        return score;
    }

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

    private Set<Integer> enumerateMonthsInclusive(LocalDate start, LocalDate end) {
        Set<Integer> set = new HashSet<>();
        if (start == null || end == null) return set;
        LocalDate cur = start;
        int guard = 0;
        while (!cur.isAfter(end) && guard++ < 370) {
            set.add(cur.getMonthValue());
            cur = cur.plusMonths(1).withDayOfMonth(1);
        }
        return set;
    }

    private static String ns(String s){ return s == null ? "" : s; }
    private static String trim(String s, int max){
        if (s == null) return null;
        String t = s.replaceAll("\\s+"," ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}