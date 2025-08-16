package com.hackathon_5.Yogiyong_In.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarGetReqDTO {
    private int year;
    private int month;
    private int date;
}