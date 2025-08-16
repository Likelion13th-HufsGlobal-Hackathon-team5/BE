package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.DTO.CalendarGetReqDTO;
import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    private final FestivalRepository festivalRepository;

    public CalendarService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    public List<Festival> getFestivalByDate(CalendarGetReqDTO requestDTO) {
        // year, month, date를 YYYY-MM-DD 문자열로 변환
        String date = String.format("%04d-%02d-%02d",
                requestDTO.getYear(),
                requestDTO.getMonth(),
                requestDTO.getDate());

        // startDate <= date <= endDate 조건으로 조회
        return festivalRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }
}
