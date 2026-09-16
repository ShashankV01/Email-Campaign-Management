package com.example.campaign.controller;

import com.example.campaign.dto.*;
import com.example.campaign.entity.CampaignStatus;
import com.example.campaign.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> create(
        @Valid @RequestBody CreateCampaignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Campaign created successfully",
                campaignService.createCampaign(request)
            ));
    }

    @PostMapping("/{campaignId}/recipients")
    public ResponseEntity<ApiResponse<java.util.List<RecipientResponse>>> addRecipients(
        @PathVariable Long campaignId,
        @Valid @RequestBody AddRecipientsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Recipients added successfully",
                campaignService.addRecipients(campaignId, request)
            ));
    }

    @PostMapping("/{campaignId}/schedule")
    public ResponseEntity<ApiResponse<CampaignResponse>> schedule(
        @PathVariable Long campaignId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Campaign scheduled successfully",
            campaignService.scheduleCampaign(campaignId)
        ));
    }

    @PostMapping("/{campaignId}/process")
    public ResponseEntity<ApiResponse<Void>> process(
        @PathVariable Long campaignId
    ) {
        campaignService.processCampaign(campaignId);
        return ResponseEntity.ok(
            ApiResponse.success("Campaign processed successfully", null)
        );
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> processDue() {
        int count = campaignService.processDueCampaigns();
        return ResponseEntity.ok(
            ApiResponse.success(
                "Due campaigns processed",
                Map.of("processedCampaigns", count)
            )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> list(
        @RequestParam(required = false) CampaignStatus status,
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "desc") String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Campaigns retrieved successfully",
            campaignService.listCampaigns(status, name, page, size, sort)
        ));
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<ApiResponse<CampaignDetailsResponse>> details(
        @PathVariable Long campaignId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Campaign details retrieved successfully",
            campaignService.getDetails(campaignId)
        ));
    }

    @GetMapping("/{campaignId}/statistics")
    public ResponseEntity<ApiResponse<StatisticsResponse>> statistics(
        @PathVariable Long campaignId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Campaign statistics retrieved successfully",
            campaignService.getStatistics(campaignId)
        ));
    }
}
