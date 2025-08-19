package com.hackathon_5.Yogiyong_In.controller;

import com.hackathon_5.Yogiyong_In.domain.Festival;
import com.hackathon_5.Yogiyong_In.service.CalendarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/festivals")
    public List<Festival> getFestivalsByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int date) {
        return calendarService.getFestivalByDate(year, month, date);
    }
}
