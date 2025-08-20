package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Festival.FestivalInfoResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final FestivalRepository festivalRepository;


    public List<FestivalInfoResDto> getAllFestivals() {
        return festivalRepository.findAll().stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    public Optional<FestivalInfoResDto> getFestivalById(Integer id) {
        return festivalRepository.findById(id)
                .map(this::toResDTO);
    }

    public List<FestivalInfoResDto> getFestivalsByDate(int year, int month, int date) {
        String strDate = String.format("%04d-%02d-%02d", year, month, date);
        return festivalRepository.findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(strDate, strDate)
                .stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    private FestivalInfoResDto toResDTO(Festival festival) {
        return FestivalInfoResDto.builder()
                .festivalId(festival.getFestivalId())
                .festivalName(festival.getFestivalName())
                .festivalDesc(festival.getFestivalDesc())
                .festivalStart(festival.getFestivalStart())
                .festivalEnd(festival.getFestivalEnd())
                .festivalLoca(festival.getFestivalLoca())
                .imagePath(festival.getImagePath())
                .aiReview(festival.getAiReview())
                .build();
    }
}