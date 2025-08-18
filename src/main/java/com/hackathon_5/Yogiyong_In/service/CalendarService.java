package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetResDto;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

        return festivalRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CalendarGetResDto toDto(Festival festival) {
        return new CalendarGetResDto(
                festival.getFestivalId(),
                festival.getName(),
                festival.getDescription(),
                festival.getStartDate().toString(),
                festival.getEndDate().toString(),
                festival.getLocation(),
                festival.getImagePath()
        );
    }
}