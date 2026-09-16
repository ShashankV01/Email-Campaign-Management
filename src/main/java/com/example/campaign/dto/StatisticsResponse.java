package com.example.campaign.dto;

public record StatisticsResponse(
    Long campaignId,
    long totalRecipients,
    long delivered,
    long failed,
    long pending
) {}
