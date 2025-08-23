package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.dto.Review.*;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.domain.Review;
import com.hackathon_5.Yogiyong_In.domain.User;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import com.hackathon_5.Yogiyong_In.repository.ReviewRepository;
import com.hackathon_5.Yogiyong_In.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;

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

    @Transactional
    public ReviewCreateResDto createReview(ReviewCreateReqDto reqDto) {
        User user = userRepository.findById(reqDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Festival festival = festivalRepository.findById(reqDto.getFestivalId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        Review review = Review.builder()
                .reviewTitle(reqDto.getReviewTitle())
                .reviewCont(reqDto.getReviewCont())
                .user(user)
                .festival(festival)
                .build();

        Review saved = reviewRepository.save(review);

        return new ReviewCreateResDto(
                saved.getReviewId(),
                saved.getReviewTitle(),
                saved.getReviewCont(),
                saved.getUser().getNickname(),
                saved.getCreatedAt()
        );
    }
}