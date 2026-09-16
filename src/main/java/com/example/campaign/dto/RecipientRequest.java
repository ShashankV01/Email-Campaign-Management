package com.example.campaign.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecipientRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Email @Size(max = 255) String email
) {}
