package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festivals")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id", nullable = false)
    private Integer festivalId;

    @Column(name = "festival_name", length = 100, nullable = false)
    private String festivalName;

    @Column(name = "festival_desc", columnDefinition = "TEXT")
    private String festivalDesc;

    @Column(name = "festival_start")
    private String festivalStart;

    @Column(name = "festival_end")
    private String festivalEnd;

    @Column(name = "festival_loca", length = 50)
    private String festivalLoca;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "ai_review", columnDefinition = "TEXT")
    private String aiReview;
}