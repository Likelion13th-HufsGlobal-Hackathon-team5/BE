package com.hackathon_5.Yogiyong_In.dto.Image;

import jakarta.validation.constraints.NotBlank;

public record ImageUrlPatchRequest(@NotBlank String url) {}
