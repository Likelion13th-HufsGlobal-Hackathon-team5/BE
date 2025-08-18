package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.ReviewGetItemResDto;
import com.hackathon_5.Yogiyong_In.DTO.ReviewGetUserResDto;
import com.hackathon_5.Yogiyong_In.DTO.ReviewScrollResDto;
import com.hackathon_5.Yogiyong_In.domain.Review;
import com.hackathon_5.Yogiyong_In.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewScrollResDto getReviewsScroll(Integer festivalId, Integer cursor, int size) {
        int s = size <= 0 ? 20 : Math.min(size, 100);

        // 커서 기반 조회 (Repository에서 커서 조건과 정렬 보장 필요)
        var slice = reviewRepository.findScrollByFestival(festivalId, cursor, PageRequest.of(0, s + 1));
        boolean hasNext = slice.size() > s;
        if (hasNext) slice = slice.subList(0, s);

        var items = slice.stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        Integer nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getReviewId();

        return ReviewScrollResDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private ReviewGetItemResDto toItem(Review r) {
        String createdIso = r.getCreatedAt() == null
                ? null
                : r.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        var userDto = ReviewGetUserResDto.builder()
                .userId(r.getUser().getUserId())
                .nickname(r.getUser().getNickname())
                .build();

        return ReviewGetItemResDto.builder()
                .reviewId(r.getReviewId())
                .user(userDto)
                .title(r.getReviewTitle())
                .content(r.getReviewCont())
                .createdAt(createdIso)
                .build();
    }
}
