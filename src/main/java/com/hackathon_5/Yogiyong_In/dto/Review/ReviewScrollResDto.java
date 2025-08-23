package com.hackathon_5.Yogiyong_In.dto.Review;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewScrollResDto {
    private List<ReviewGetItemResDto> items; // [{reviewId, reviewTitle, reviewCont, createdAt, user{...}}]
    private Integer nextCursor;               // 다음 시작 reviewId (없으면 null)
    private boolean hasNext;
}
