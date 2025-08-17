package com.hackathon_5.Yogiyong_In.config;

import com.hackathon_5.Yogiyong_In.DTO.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e){
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e){
        var msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getField()+": "+f.getDefaultMessage()).orElse("Validation error");
        return ApiResponse.fail(msg);
    }
}