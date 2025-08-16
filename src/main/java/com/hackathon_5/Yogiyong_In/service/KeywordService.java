package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.KeywordGetItemDto;
import com.hackathon_5.Yogiyong_In.DTO.KeywordScrollResDto;
import com.hackathon_5.Yogiyong_In.domain.Keyword;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.domain.UserKeyword;
import com.hackathon_5.Yogiyong_In.repository.KeywordRepository;
import com.hackathon_5.Yogiyong_In.repository.UserKeywordRepository;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;

    // 예: "https://cdn.example.com". 비워두면 toIconUrl에서 상대경로 처리
    private static final String ICON_BASE = "";

    // =====================================================
    // 유저 식별: 로그인(JWT) 우선, 아니면 deviceId 기반 게스트
    // =====================================================
    private String resolveUserId(String authHeader, String deviceId) {
        log.debug("[resolveUserId] authHeader='{}', deviceId='{}'", authHeader, deviceId);

        // 1) 로그인 사용자 우선 (TODO: 실제 JWT 파싱으로 교체)
        String userId = extractUserIdOrNull(authHeader);
        log.debug("[resolveUserId] extractUserIdOrNull -> {}", userId);
        if (userId != null) return userId;

        // 2) 비로그인: Device-Id 기반 게스트 생성/보장
        if (deviceId != null && !deviceId.isBlank()) {
            log.debug("[resolveUserId] deviceId 존재 -> ensureGuestUser");
            return ensureGuestUser(deviceId);
        }

        // 3) 둘 다 없으면 400 성격
        log.error("[resolveUserId] Authorization/X-Device-Id 둘 다 없음 -> IllegalArgumentException");
        throw new IllegalArgumentException("Authorization 또는 X-Device-Id 헤더가 필요합니다.");
    }

    // 게스트 user_id를 20자 이내로 안전 축약: "g:" + sha256(deviceId) 12자리
    private String compactGuestId(String deviceId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(deviceId.getBytes(StandardCharsets.UTF_8));
            String hex = java.util.HexFormat.of().formatHex(digest);
            String shortHex = hex.substring(0, 12);
            String result = "g:" + shortHex; // 총 14자
            log.debug("[compactGuestId] deviceId='{}' -> '{}'", deviceId, result);
            return result;
        } catch (Exception e) {
            String fallback = "g:" + Integer.toHexString(deviceId.hashCode());
            log.warn("[compactGuestId] SHA-256 실패 -> fallback '{}', reason={}", fallback, e.toString());
            return fallback;
        }
    }

    /**
     * 게스트 유저를 보장/생성한다.
     * public + @Transactional 로 둬서 프록시 트랜잭션이 적용되도록 함.
     */
    @Transactional
    public String ensureGuestUser(String deviceId) {
        log.debug("[ensureGuestUser] START deviceId='{}'", deviceId);
        String userId = "guest:" + deviceId;
        if (userId.length() > 20) {
            userId = compactGuestId(deviceId); // 20자 이내 보장
        }
        if (userRepository.existsById(userId)) {
            log.debug("[ensureGuestUser] 이미 존재 -> userId='{}'", userId);
            return userId;
        }

        String base = "guest_" + Math.abs(deviceId.hashCode());
        String nick = base.substring(0, Math.min(20, base.length()));

        // 닉네임 UNIQUE 충돌에 대비해 최대 5회 재시도
        for (int i = 0; i < 5; i++) {
            try {
                User user = User.builder()
                        .userId(userId)
                        .password(UUID.randomUUID().toString()) // NOT NULL 충족 + 의미 없는 랜덤 문자열
                        .nickname(nick)
                        .birthYear(null)
                        .build();
                userRepository.saveAndFlush(user);
                log.info("[ensureGuestUser] 게스트 생성 성공 userId='{}', nickname='{}'", userId, nick);
                return userId;
            } catch (DataIntegrityViolationException dup) {
                log.warn("[ensureGuestUser] 닉네임 충돌 재시도 idx={}, base='{}'", i, base);
                String suffix = "_" + i;
                int cut = Math.max(0, 20 - suffix.length());
                nick = base.substring(0, Math.min(cut, base.length())) + suffix;
            }
        }

        // 최후의 수단: 랜덤 닉네임
        String rndNick = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        User user = User.builder()
                .userId(userId)
                .password(UUID.randomUUID().toString())
                .nickname(rndNick)
                .birthYear(null)
                .build();
        userRepository.save(user);
        log.info("[ensureGuestUser] 랜덤 닉네임으로 최종 생성 userId='{}', nickname='{}'", userId, rndNick);
        return userId;
    }

    // ===========================================
    // [기존 유지] authHeader만 받는 버전 (로그인 전제)
    // ===========================================
    @Transactional(readOnly = true)
    public KeywordScrollResDto getKeywordsScroll(Integer cursor, int size, boolean includeSelected, String authHeader) {
        log.debug("[getKeywordsScroll-basic] cursor={}, size={}, includeSelected={}, authHeader='{}'",
                cursor, size, includeSelected, authHeader);

        int s = size <= 0 ? 30 : Math.min(size, 100);

        List<Keyword> slice = keywordRepository.findScroll(cursor, PageRequest.of(0, s + 1));
        boolean hasNext = slice.size() > s;
        if (hasNext) slice = slice.subList(0, s);
        log.debug("[getKeywordsScroll-basic] slice.size={}, hasNext={}", slice.size(), hasNext);

        String userId = extractUserIdOrNull(authHeader);
        log.debug("[getKeywordsScroll-basic] resolved userId='{}'", userId);

        Set<Integer> selectedIds = Collections.emptySet();

        if (includeSelected && userId != null && !slice.isEmpty()) {
            List<Integer> ids = slice.stream().map(Keyword::getKeywordId).toList();
            log.debug("[getKeywordsScroll-basic] includeSelected ids={}", ids);
            selectedIds = userKeywordRepository.findSelectedIn(userId, ids).stream()
                    .map(uk -> uk.getKeyword().getKeywordId())
                    .collect(Collectors.toSet());
            log.debug("[getKeywordsScroll-basic] selectedIds={}", selectedIds);
        }

        final Set<Integer> finalSelectedIds = selectedIds;

        List<KeywordGetItemDto> items = slice.stream()
                .map(k -> KeywordGetItemDto.builder()
                        .keywordId(k.getKeywordId())
                        .name(k.getKeywordName())
                        .iconUrl(toIconUrl(k.getImagePath()))
                        .selected(includeSelected && userId != null
                                ? finalSelectedIds.contains(k.getKeywordId())
                                : null)
                        .build())
                .toList();

        Integer nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getKeywordId();
        log.debug("[getKeywordsScroll-basic] items={}, nextCursor={}", items.size(), nextCursor);

        return KeywordScrollResDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    // ===================================================
    // [개선] deviceId 포함 버전: includeSelected일 때만 게스트 보장(Lazy)
    // ===================================================
    @Transactional(readOnly = true)
    public KeywordScrollResDto getKeywordsScroll(
            Integer cursor, int size, boolean includeSelected,
            String authHeader, String deviceId
    ) {
        log.debug("[getKeywordsScroll-extended] cursor={}, size={}, includeSelected={}, authHeader='{}', deviceId='{}'",
                cursor, size, includeSelected, authHeader, deviceId);

        final String uid;
        if (includeSelected) {
            // 선택표시 계산이 필요할 때에만 userId 해석/게스트 보장
            String tmp;
            try {
                tmp = resolveUserId(authHeader, deviceId);
            } catch (IllegalArgumentException e) {
                log.warn("[getKeywordsScroll-extended] userId 해석 실패(includeSelected=true) -> 선택표시 미표시, reason={}", e.getMessage());
                tmp = null; // 선택표시 불가
            }
            uid = tmp;
        } else {
            uid = null; // 조회만 할 때는 게스트 생성 안 함
        }
        log.debug("[getKeywordsScroll-extended] resolved uid='{}'", uid);

        int s = size <= 0 ? 30 : Math.min(size, 100);

        List<Keyword> slice = keywordRepository.findScroll(cursor, PageRequest.of(0, s + 1));
        boolean hasNext = slice.size() > s;
        if (hasNext) slice = slice.subList(0, s);
        log.debug("[getKeywordsScroll-extended] slice.size={}, hasNext={}", slice.size(), hasNext);

        Set<Integer> selectedIds = Collections.emptySet();
        if (includeSelected && uid != null && !slice.isEmpty()) {
            List<Integer> ids = slice.stream().map(Keyword::getKeywordId).toList();
            log.debug("[getKeywordsScroll-extended] includeSelected ids={}", ids);
            selectedIds = userKeywordRepository.findSelectedIn(uid, ids).stream()
                    .map(uk -> uk.getKeyword().getKeywordId())
                    .collect(Collectors.toSet());
            log.debug("[getKeywordsScroll-extended] selectedIds={}", selectedIds);
        }
        final Set<Integer> finalSelectedIds = selectedIds;

        List<KeywordGetItemDto> items = slice.stream()
                .map(k -> KeywordGetItemDto.builder()
                        .keywordId(k.getKeywordId())
                        .name(k.getKeywordName())
                        .iconUrl(toIconUrl(k.getImagePath()))
                        .selected(includeSelected && uid != null
                                ? finalSelectedIds.contains(k.getKeywordId())
                                : null)
                        .build())
                .toList();

        Integer nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getKeywordId();
        log.debug("[getKeywordsScroll-extended] items={}, nextCursor={}", items.size(), nextCursor);

        return KeywordScrollResDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    // ===========================================
    // 선택 교체(업서트): 전체 false → 전달된 것만 true
    // ===========================================
    @Transactional
    public void replaceUserKeywords(String userId, List<Integer> keywordIds) {
        log.debug("[replaceUserKeywords-direct] START userId='{}', keywordIds={}", userId, keywordIds);

        // 전체 해제
        int updated = userKeywordRepository.clearSelections(userId);
        log.debug("[replaceUserKeywords-direct] clearSelections updatedRows={}", updated);

        if (keywordIds == null || keywordIds.isEmpty()) {
            log.debug("[replaceUserKeywords-direct] keywordIds 비어있음 -> END");
            return;
        }

        // 전달된 것만 선택 true로 업서트
        for (Integer kid : keywordIds) {
            log.debug("[replaceUserKeywords-direct] upsert keywordId={}", kid);
            var existing = userKeywordRepository.findByUser_UserIdAndKeyword_KeywordId(userId, kid);
            if (existing.isPresent()) {
                log.trace("[replaceUserKeywords-direct] 기존 존재 -> set isSelected=true & save");
                existing.get().setIsSelected(true);
                userKeywordRepository.save(existing.get());
            } else {
                log.trace("[replaceUserKeywords-direct] 신규 생성 -> save");
                UserKeyword uk = new UserKeyword();
                uk.setUser(User.builder().userId(userId).build());
                uk.setKeyword(Keyword.builder().keywordId(kid).build());
                uk.setIsSelected(true);
                userKeywordRepository.save(uk);
            }
        }
        log.debug("[replaceUserKeywords-direct] END");
    }

    // headers → userId 해석 뒤 기존 메서드 재사용
    @Transactional
    public void replaceUserKeywords(String authHeader, String deviceId, List<Integer> keywordIds) {
        log.debug("[replaceUserKeywords-header] START authHeader='{}', deviceId='{}', keywordIds={}",
                authHeader, deviceId, keywordIds);
        String userId = resolveUserId(authHeader, deviceId);
        log.debug("[replaceUserKeywords-header] resolved userId='{}'", userId);
        replaceUserKeywords(userId, keywordIds);
        log.debug("[replaceUserKeywords-header] END");
    }

    // ===========================================
    // 선택된 키워드 이름까지 조회
    // ===========================================
    @Transactional(readOnly = true)
    public List<KeywordGetItemDto> getSelectedKeywordsWithNames(String userId) {
        log.debug("[getSelectedKeywordsWithNames-direct] userId='{}'", userId);

        var selectedList = userKeywordRepository.findByUser_UserIdAndIsSelectedTrue(userId);
        log.debug("[getSelectedKeywordsWithNames-direct] selectedList.size={}", selectedList == null ? 0 : selectedList.size());
        if (selectedList == null || selectedList.isEmpty()) return List.of();

        List<Integer> ids = selectedList.stream()
                .map(uk -> uk.getKeyword().getKeywordId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        log.debug("[getSelectedKeywordsWithNames-direct] ids={}", ids);

        if (ids.isEmpty()) return List.of();

        List<Keyword> keywords = keywordRepository.findByKeywordIdIn(ids);
        keywords.sort(Comparator.comparing(Keyword::getKeywordId));
        log.debug("[getSelectedKeywordsWithNames-direct] fetched keywords.size={}", keywords.size());

        List<KeywordGetItemDto> result = keywords.stream()
                .map(k -> KeywordGetItemDto.builder()
                        .keywordId(k.getKeywordId())
                        .name(k.getKeywordName())
                        .iconUrl(toIconUrl(k.getImagePath()))
                        .selected(true)
                        .build())
                .toList();
        log.debug("[getSelectedKeywordsWithNames-direct] result.size={}", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<KeywordGetItemDto> getSelectedKeywordsWithNames(String authHeader, String deviceId) {
        log.debug("[getSelectedKeywordsWithNames-header] START authHeader='{}', deviceId='{}'", authHeader, deviceId);
        String userId = resolveUserId(authHeader, deviceId);
        List<KeywordGetItemDto> result = getSelectedKeywordsWithNames(userId);
        log.debug("[getSelectedKeywordsWithNames-header] END userId='{}', result.size={}", userId, result.size());
        return result;
    }

    // =========================
    // 헬퍼
    // =========================
    private String extractUserIdOrNull(String authHeader) {
        log.debug("[extractUserIdOrNull] rawHeader='{}'", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring("Bearer ".length());
        String uid = token.isBlank() ? null : "demoUser"; // TODO: JWT 파싱으로 교체
        log.debug("[extractUserIdOrNull] token='{}' -> uid='{}'", token, uid);
        return uid;
    }

    private String toIconUrl(String imagePath) {
        log.trace("[toIconUrl] imagePath='{}'", imagePath);
        if (imagePath == null || imagePath.isBlank()) return null;
        if (imagePath.startsWith("http")) return imagePath;

        if (ICON_BASE == null || ICON_BASE.isBlank()) {
            // 상대경로 반환 (선행 슬래시 보장)
            String res = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            log.trace("[toIconUrl] ICON_BASE blank -> '{}'", res);
            return res;
        }
        boolean needSlash = !ICON_BASE.endsWith("/") && !imagePath.startsWith("/");
        String res = ICON_BASE + (needSlash ? "/" : "") + imagePath;
        log.trace("[toIconUrl] joined -> '{}'", res);
        return res;
    }
}
