package com.example.campaign.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateCampaignRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 255) String subject,
    @NotBlank @Email @Size(max = 255) String senderEmail,
    @NotBlank String content,
    @NotNull @Future LocalDateTime scheduledAt
) {}
