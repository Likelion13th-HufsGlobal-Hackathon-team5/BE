package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.Keyword;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.dto.Festival.FestivalsByKeywordResDto;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalAiReviewService festivalAiReviewService;
    private final KeywordRepository keywordRepository;

    // --- 기존 getFestivalDetail 메소드는 그대로 유지 ---
    public FestivalInfoResDto getFestivalDetail(Integer id) {
        Festival f = festivalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));
        String aiReview = festivalAiReviewService.generateFestivalReview(f.getFestivalDesc());
        return FestivalInfoResDto.fromEntity(f, aiReview);
    }

    @Transactional(readOnly = true)
    public List<FestivalsByKeywordResDto> findFestivalsGroupedByKeyword(List<Integer> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 요청된 키워드 ID에 해당하는 키워드 정보(ID, 이름)를 DB에서 가져옵니다.
        List<Keyword> keywords = keywordRepository.findByKeywordIdIn(keywordIds);

        // 2. 각 키워드별로 해당하는 축제 목록을 찾아 그룹으로 묶습니다.
        return keywords.stream()
                .map(keyword -> {
                    // 3. 현재 키워드에 해당하는 축제 목록을 DB에서 조회합니다.
                    List<Festival> festivals = festivalRepository.findByKeywords_KeywordId(keyword.getKeywordId());

                    // 4. 축제 목록을 API 응답 형식(DTO)으로 변환합니다.
                    List<FestivalInfoResDto> festivalDtos = festivals.stream()
                            .map(festival -> FestivalInfoResDto.fromEntity(festival, null))
                            .collect(Collectors.toList());

                    // 5. 최종적으로 '키워드 정보 + 해당 축제 목록' 형태로 조립하여 반환합니다.
                    return FestivalsByKeywordResDto.builder()
                            .keywordId(keyword.getKeywordId())
                            .keywordName(keyword.getKeywordName())
                            .festivals(festivalDtos)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
