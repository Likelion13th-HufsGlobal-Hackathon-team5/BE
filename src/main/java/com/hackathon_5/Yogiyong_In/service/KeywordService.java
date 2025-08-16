package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.KeywordGetItemDto;
import com.hackathon_5.Yogiyong_In.DTO.KeywordScrollResDto;
import com.hackathon_5.Yogiyong_In.domain.Keyword;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.domain.UserKeyword;
import com.hackathon_5.Yogiyong_In.repository.KeywordRepository;
import com.hackathon_5.Yogiyong_In.repository.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserKeywordRepository userKeywordRepository;

    private static final String ICON_BASE = ""; // 예: "https://cdn.example.com"

    /** 컨트롤러가 전달하는 userIdOrNull(비로그인이면 null) 기반으로 동작 */
    public KeywordScrollResDto getKeywordsScroll(Integer cursor, int size, boolean includeSelected, String userIdOrNull) {
        int s = size <= 0 ? 30 : Math.min(size, 100);

        // 커서 기반 슬라이스 조회 (레포에 findScroll(cursor, pageable) 있다고 가정)
        List<Keyword> slice = keywordRepository.findScroll(cursor, PageRequest.of(0, s + 1));
        boolean hasNext = slice.size() > s;
        if (hasNext) slice = slice.subList(0, s);

        Set<Integer> selectedIds = Collections.emptySet();
        if (includeSelected && userIdOrNull != null && !slice.isEmpty()) {
            List<Integer> ids = slice.stream().map(Keyword::getKeywordId).toList();

            // 표준 메서드로 교체(레포에 없으면 추가): findByUser_UserIdAndKeyword_KeywordIdIn
            selectedIds = userKeywordRepository
                    .findByUser_UserIdAndKeyword_KeywordIdIn(userIdOrNull, ids).stream()
                    .filter(UserKeyword::getIsSelected)
                    .map(uk -> uk.getKeyword().getKeywordId())
                    .collect(Collectors.toSet());
        }

        final Set<Integer> finalSelectedIds = selectedIds;

        List<KeywordGetItemDto> items = slice.stream()
                .map(k -> KeywordGetItemDto.builder()
                        .keywordId(k.getKeywordId())
                        .name(k.getKeywordName())
                        .iconUrl(toIconUrl(k.getImagePath()))
                        .selected(includeSelected && userIdOrNull != null
                                ? finalSelectedIds.contains(k.getKeywordId())
                                : null)
                        .build())
                .toList();

        Integer nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getKeywordId();

        return KeywordScrollResDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    /** 저장은 인증 전제: 컨트롤러에서 이미 userId를 보장 */
    @Transactional
    public void replaceUserKeywords(String userId, List<Integer> keywordIds) {
        // 전체 해제
        userKeywordRepository.clearSelections(userId);

        List<Integer> ids = (keywordIds == null) ? List.of()
                : keywordIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;

        // 존재 검증(잘못된 ID는 400)
        Set<Integer> existing = keywordRepository.findByKeywordIdIn(ids).stream()
                .map(Keyword::getKeywordId)
                .collect(Collectors.toSet());
        List<Integer> bad = ids.stream().filter(id -> !existing.contains(id)).toList();
        if (!bad.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "invalid keywordIds: " + bad);
        }

        // 전달된 것만 true로 업서트
        for (Integer kid : ids) {
            var opt = userKeywordRepository.findByUser_UserIdAndKeyword_KeywordId(userId, kid);
            UserKeyword uk = opt.orElseGet(() -> UserKeyword.builder()
                    .user(User.builder().userId(userId).build())
                    .keyword(Keyword.builder().keywordId(kid).build())
                    .isSelected(true)
                    .build());
            uk.setIsSelected(true);
            userKeywordRepository.save(uk);
        }
    }

    @Transactional(readOnly = true)
    public List<KeywordGetItemDto> getSelectedKeywordsWithNames(String userId) {
        var selectedList = userKeywordRepository.findByUser_UserIdAndIsSelectedTrue(userId);
        if (selectedList == null || selectedList.isEmpty()) return List.of();

        List<Integer> ids = selectedList.stream()
                .map(uk -> uk.getKeyword().getKeywordId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) return List.of();

        List<Keyword> keywords = keywordRepository.findByKeywordIdIn(ids);
        keywords.sort(Comparator.comparing(Keyword::getKeywordId));

        return keywords.stream()
                .map(k -> KeywordGetItemDto.builder()
                        .keywordId(k.getKeywordId())
                        .name(k.getKeywordName())
                        .iconUrl(toIconUrl(k.getImagePath()))
                        .selected(true)
                        .build())
                .toList();
    }

    private String toIconUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        if (imagePath.startsWith("http")) return imagePath;
        return ICON_BASE + imagePath;
    }
}
