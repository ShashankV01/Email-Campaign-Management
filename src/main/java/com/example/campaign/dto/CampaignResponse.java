package com.example.campaign.dto;

import com.example.campaign.entity.Campaign;
import java.time.LocalDateTime;

public record CampaignResponse(
    Long id,
    String name,
    String subject,
    String senderEmail,
    String content,
    LocalDateTime scheduledAt,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CampaignResponse from(Campaign c) {
        return new CampaignResponse(
            c.getId(), c.getName(), c.getSubject(), c.getSenderEmail(),
            c.getContent(), c.getScheduledAt(), c.getStatus().name(),
            c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}
