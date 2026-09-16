package com.example.campaign.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddRecipientsRequest(
    @NotEmpty @Valid List<RecipientRequest> recipients
) {}
