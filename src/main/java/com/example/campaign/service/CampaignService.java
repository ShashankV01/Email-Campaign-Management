package com.example.campaign.service;

import com.example.campaign.dto.*;
import com.example.campaign.entity.*;
import com.example.campaign.exception.BusinessException;
import com.example.campaign.exception.NotFoundException;
import com.example.campaign.repository.CampaignRepository;
import com.example.campaign.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@Service
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private final CampaignRepository campaignRepository;
    private final RecipientRepository recipientRepository;
    private final Random random = new Random();

    public CampaignService(CampaignRepository campaignRepository,
                           RecipientRepository recipientRepository) {
        this.campaignRepository = campaignRepository;
        this.recipientRepository = recipientRepository;
    }

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.name().trim());
        campaign.setSubject(request.subject().trim());
        campaign.setSenderEmail(request.senderEmail().trim().toLowerCase(Locale.ROOT));
        campaign.setContent(request.content());
        campaign.setScheduledAt(request.scheduledAt());
        campaign.setStatus(CampaignStatus.DRAFT);

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    @Transactional
    public List<RecipientResponse> addRecipients(Long campaignId, AddRecipientsRequest request) {
        Campaign campaign = getCampaign(campaignId);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BusinessException("Recipients can only be added to a draft campaign");
        }

        Set<String> requestEmails = new HashSet<>();

        for (RecipientRequest item : request.recipients()) {
            String email = item.email().trim().toLowerCase(Locale.ROOT);

            if (!requestEmails.add(email)) {
                throw new BusinessException("Duplicate email in request: " + email);
            }

            if (recipientRepository.existsByCampaignIdAndEmailIgnoreCase(campaignId, email)) {
                throw new BusinessException("Recipient already exists: " + email);
            }
        }

        List<Recipient> saved = request.recipients().stream().map(item -> {
            Recipient r = new Recipient();
            r.setName(item.name().trim());
            r.setEmail(item.email().trim().toLowerCase(Locale.ROOT));
            r.setStatus(RecipientStatus.PENDING);
            r.setCampaign(campaign);
            return r;
        }).toList();

        try {
            return recipientRepository.saveAll(saved)
                .stream().map(RecipientResponse::from).toList();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("One or more recipients already exist");
        }
    }

    @Transactional
    public CampaignResponse scheduleCampaign(Long campaignId) {
        Campaign campaign = getCampaign(campaignId);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BusinessException("Only a draft campaign can be scheduled");
        }

        if (campaign.getRecipients().isEmpty()) {
            throw new BusinessException("Campaign must have at least one recipient");
        }

        if (campaign.getScheduledAt() == null ||
            !campaign.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Scheduled time must be in the future");
        }

        campaign.setStatus(CampaignStatus.SCHEDULED);
        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    @Transactional
    public void processCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findByIdForUpdate(campaignId)
            .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));

        if (campaign.getStatus() != CampaignStatus.SCHEDULED) {
            throw new BusinessException("Only scheduled campaigns can be processed");
        }

        if (campaign.getScheduledAt() == null ||
            campaign.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Campaign is not due for processing");
        }

        log.info("Processing campaign id={}", campaignId);

        for (Recipient recipient : campaign.getRecipients()) {
            if (recipient.getStatus() != RecipientStatus.PENDING) {
                continue;
            }

            recipient.setStatus(
                random.nextBoolean()
                    ? RecipientStatus.DELIVERED
                    : RecipientStatus.FAILED
            );
        }

        campaign.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(campaign);

        log.info("Completed campaign id={}", campaignId);
    }

    @Transactional
    public int processDueCampaigns() {
        List<Campaign> due = campaignRepository.findDueCampaigns(
            CampaignStatus.SCHEDULED,
            LocalDateTime.now(),
            PageRequest.of(0, 50)
        ).getContent();

        int processed = 0;
        for (Campaign campaign : due) {
            try {
                processCampaign(campaign.getId());
                processed++;
            } catch (BusinessException ex) {
                log.warn("Could not process campaign {}: {}", campaign.getId(), ex.getMessage());
            }
        }
        return processed;
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> listCampaigns(
        CampaignStatus status,
        String name,
        int page,
        int size,
        String sortDirection
    ) {
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        Sort.Direction direction =
            "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
            page, size, Sort.by(direction, "createdAt")
        );

        return campaignRepository.search(
            status,
            name == null || name.isBlank() ? null : name.trim(),
            pageable
        ).map(CampaignResponse::from);
    }

    @Transactional(readOnly = true)
    public CampaignDetailsResponse getDetails(Long campaignId) {
        return CampaignDetailsResponse.from(getCampaign(campaignId));
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(Long campaignId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NotFoundException("Campaign not found: " + campaignId);
        }

        long total = recipientRepository.countByCampaignId(campaignId);
        long delivered = recipientRepository.countByCampaignIdAndStatus(
            campaignId, RecipientStatus.DELIVERED
        );
        long failed = recipientRepository.countByCampaignIdAndStatus(
            campaignId, RecipientStatus.FAILED
        );
        long pending = recipientRepository.countByCampaignIdAndStatus(
            campaignId, RecipientStatus.PENDING
        );

        return new StatisticsResponse(
            campaignId, total, delivered, failed, pending
        );
    }

    @Transactional(readOnly = true)
    public Campaign getCampaign(Long campaignId) {
        return campaignRepository.findById(campaignId)
            .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));
    }
}
