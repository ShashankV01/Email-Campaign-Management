package com.example.campaign.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CampaignScheduler {

    private static final Logger log = LoggerFactory.getLogger(CampaignScheduler.class);
    private final CampaignService campaignService;

    public CampaignScheduler(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Scheduled(fixedDelayString = "${CAMPAIGN_PROCESS_INTERVAL_MS:30000}")
    public void processDueCampaigns() {
        int count = campaignService.processDueCampaigns();
        if (count > 0) {
            log.info("Automatically processed {} campaign(s)", count);
        }
    }
}
