package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.CalendarGetReqDto;
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
    public List<Festival> getFestivalByDate(CalendarGetReqDto requestDTO) {
        LocalDate date = LocalDate.of(
                requestDTO.getYear(),
                requestDTO.getMonth(),
                requestDTO.getDate()
        );

        return festivalRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }
}
