package com.hackathon_5.Yogiyong_In.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Festival {
    private Long festivalId;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String location;
    private String imagePath;
}
