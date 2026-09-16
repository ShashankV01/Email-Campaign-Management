package com.example.campaign.dto;

import com.example.campaign.entity.Recipient;

public record RecipientResponse(
    Long id,
    String name,
    String email,
    String status
) {
    public static RecipientResponse from(Recipient r) {
        return new RecipientResponse(r.getId(), r.getName(), r.getEmail(), r.getStatus().name());
    }
}
