package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetReqDto;
import com.hackathon_5.Yogiyong_In.DTO.Calendar.CalendarGetResDto;
import com.hackathon_5.Yogiyong_In.service.CalendarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping("/festivals")
    public List<CalendarGetResDto> getFestivalsByDate(@Valid @RequestBody CalendarGetReqDto dto) {
        return calendarService.getFestivalByDate(dto);
    }
}