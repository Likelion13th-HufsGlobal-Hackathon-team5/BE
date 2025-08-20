package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final FestivalRepository festivalRepository;

    public CalendarService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    @Transactional(readOnly = true)
    public List<CalendarGetResDto> getFestivalByDate(CalendarGetReqDto requestDTO) {
        LocalDate date = LocalDate.of(
                requestDTO.getYear(),
                requestDTO.getMonth(),
                requestDTO.getDate()
        );

        return festivalRepository
                .findByFestivalStartLessThanEqualAndFestivalEndGreaterThanEqual(date, date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CalendarGetResDto toDto(Festival f) {
        return new CalendarGetResDto(
                f.getFestivalId(),
                f.getFestivalName(),
                f.getFestivalDesc(),
                f.getFestivalStart() != null ? f.getFestivalStart().toString() : null,
                f.getFestivalEnd()   != null ? f.getFestivalEnd().toString()   : null,
                f.getFestivalLoca(),
                f.getImagePath()
        );
    }
}
