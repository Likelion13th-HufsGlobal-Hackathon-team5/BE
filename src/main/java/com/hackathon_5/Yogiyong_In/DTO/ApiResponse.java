package com.hackathon_5.Yogiyong_In.DTO;
import lombok.*;

@Getter @AllArgsConstructor @NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> ok(T data){ return new ApiResponse<>(true, data, null); }
    public static <T> ApiResponse<T> fail(String message){ return new ApiResponse<>(false, null, message); }
}