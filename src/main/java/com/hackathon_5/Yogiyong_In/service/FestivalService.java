package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalAiReviewService festivalAiReviewService;

    public FestivalInfoResDto getFestivalDetail(Integer id) {
        Festival f = festivalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        String aiReview = festivalAiReviewService.generateFestivalReview(f.getFestivalDesc());
        return FestivalInfoResDto.fromEntity(f, aiReview);
    }
}
