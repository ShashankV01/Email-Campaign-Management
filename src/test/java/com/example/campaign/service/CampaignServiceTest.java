package com.example.campaign.service;

import com.example.campaign.dto.*;
import com.example.campaign.entity.*;
import com.example.campaign.exception.BusinessException;
import com.example.campaign.repository.CampaignRepository;
import com.example.campaign.repository.RecipientRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CampaignServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock RecipientRepository recipientRepository;

    @InjectMocks CampaignService campaignService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateValidCampaign() {
        CreateCampaignRequest request = new CreateCampaignRequest(
            "Test", "Subject", "sender@example.com", "Hello",
            LocalDateTime.now().plusDays(1)
        );

        when(campaignRepository.save(any(Campaign.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.createCampaign(request);

        assertEquals("Test", response.name());
        assertEquals("DRAFT", response.status());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void shouldRejectDuplicateRecipient() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.DRAFT);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(recipientRepository.existsByCampaignIdAndEmailIgnoreCase(
            eq(1L), eq("test@example.com")
        )).thenReturn(true);

        AddRecipientsRequest request = new AddRecipientsRequest(List.of(
            new RecipientRequest("Test", "test@example.com")
        ));

        assertThrows(
            BusinessException.class,
            () -> campaignService.addRecipients(1L, request)
        );

        verify(recipientRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectSchedulingWithoutRecipients() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setScheduledAt(LocalDateTime.now().plusDays(1));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(
            BusinessException.class,
            () -> campaignService.scheduleCampaign(1L)
        );
    }

    @Test
    void shouldNotProcessAlreadyCompletedCampaign() {
        Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.COMPLETED);

        when(campaignRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(campaign));

        assertThrows(
            BusinessException.class,
            () -> campaignService.processCampaign(1L)
        );
    }

    @Test
    void shouldReturnCorrectStatistics() {
        when(campaignRepository.existsById(1L)).thenReturn(true);
        when(recipientRepository.countByCampaignId(1L)).thenReturn(10L);
        when(recipientRepository.countByCampaignIdAndStatus(
            1L, RecipientStatus.DELIVERED)).thenReturn(6L);
        when(recipientRepository.countByCampaignIdAndStatus(
            1L, RecipientStatus.FAILED)).thenReturn(3L);
        when(recipientRepository.countByCampaignIdAndStatus(
            1L, RecipientStatus.PENDING)).thenReturn(1L);

        StatisticsResponse stats = campaignService.getStatistics(1L);

        assertEquals(10, stats.totalRecipients());
        assertEquals(6, stats.delivered());
        assertEquals(3, stats.failed());
        assertEquals(1, stats.pending());
    }
}
