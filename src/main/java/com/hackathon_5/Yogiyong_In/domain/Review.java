package com.hackathon_5.Yogiyong_In.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_user_id", columnList = "user_id"),
                @Index(name = "idx_reviews_festival_id", columnList = "festival_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Integer reviewId;

    @Column(name = "review_title", length = 100, nullable = false)
    private String reviewTitle;

    @Column(name = "review_cont", columnDefinition = "TEXT", nullable = false)
    private String reviewCont;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // ERD FK
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_id", nullable = false) // ERD FK
    private Festival festival;
}

