package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.FestivalInfoReqDTO;
import com.hackathon_5.Yogiyong_In.DTO.FestivalInfoResDTO;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public List<FestivalInfoResDTO> getAllFestivals() {
        return festivalRepository.findAll().stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    public Optional<FestivalInfoResDTO> getFestivalById(Long id) {
        return festivalRepository.findById(id)
                .map(this::toResDTO);
    }

    public FestivalInfoResDTO saveFestival(FestivalInfoReqDTO reqDTO) {
        Festival festival = Festival.builder()
                .festivalName(reqDTO.getFestivalName())
                .festivalDesc(reqDTO.getFestivalDesc())
                .festivalStart(reqDTO.getFestivalStart())
                .festivalEnd(reqDTO.getFestivalEnd())
                .festivalLoca(reqDTO.getFestivalLoca())
                .imagePath(reqDTO.getImagePath())
                .aiReview(reqDTO.getAiReview())
                .build();

        Festival saved = festivalRepository.save(festival);
        return toResDTO(saved);
    }

    public void deleteFestival(Long id) {
        festivalRepository.deleteById(id);
    }

    private FestivalInfoResDTO toResDTO(Festival festival) {
        return FestivalInfoResDTO.builder()
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
