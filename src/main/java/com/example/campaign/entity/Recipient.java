package com.example.campaign.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "recipients",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_campaign_recipient_email",
        columnNames = {"campaign_id", "email"}
    )
)
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecipientStatus status = RecipientStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public RecipientStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(RecipientStatus status) { this.status = status; }
}
