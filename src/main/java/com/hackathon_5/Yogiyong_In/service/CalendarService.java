package com.hackathon_5.Yogiyong_In.service;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.repository.FestivalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final FestivalRepository festivalRepository;

    public CalendarService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    public List<Festival> getFestivalByDate(int year, int month, int date) {
        // year, month, date → YYYY-MM-DD 포맷 문자열로 변환
        String strDate = String.format("%04d-%02d-%02d", year, month, date);

        // startDate <= date <= endDate 조건으로 조회
        return festivalRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(strDate, strDate);

    }
}
