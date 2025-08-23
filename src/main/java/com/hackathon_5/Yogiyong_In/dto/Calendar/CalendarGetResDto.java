package com.hackathon_5.Yogiyong_In.dto.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class CalendarGetResDto {
    private Integer festivalId;
    private String festivalName;
    private String festivalDesc;
    private String festivalStart;
    private String festivalEnd;
    private String festivalLoca;
    private String imagePath; // 원본
   }
