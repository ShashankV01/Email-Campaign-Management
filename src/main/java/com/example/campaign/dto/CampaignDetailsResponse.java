package com.example.campaign.dto;

import com.example.campaign.entity.Campaign;
import java.util.List;

public record CampaignDetailsResponse(
    CampaignResponse campaign,
    List<RecipientResponse> recipients
) {
    public static CampaignDetailsResponse from(Campaign c) {
        return new CampaignDetailsResponse(
            CampaignResponse.from(c),
            c.getRecipients().stream().map(RecipientResponse::from).toList()
        );
    }
}
