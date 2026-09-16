package com.example.campaign.repository;

import com.example.campaign.entity.Recipient;
import com.example.campaign.entity.RecipientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    boolean existsByCampaignIdAndEmailIgnoreCase(Long campaignId, String email);
    long countByCampaignId(Long campaignId);
    long countByCampaignIdAndStatus(Long campaignId, RecipientStatus status);
}
