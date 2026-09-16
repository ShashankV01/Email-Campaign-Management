package com.example.campaign.repository;

import com.example.campaign.entity.Campaign;
import com.example.campaign.entity.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Query("""
        select c from Campaign c
        where (:status is null or c.status = :status)
          and (:name is null or lower(c.name) like lower(concat('%', :name, '%')))
        """)
    Page<Campaign> search(
        @Param("status") CampaignStatus status,
        @Param("name") String name,
        Pageable pageable
    );

    @Query("""
        select c from Campaign c
        where c.status = :status and c.scheduledAt <= :now
        order by c.scheduledAt asc
        """)
    Page<Campaign> findDueCampaigns(
        @Param("status") CampaignStatus status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Campaign c where c.id = :id")
    Optional<Campaign> findByIdForUpdate(@Param("id") Long id);
}
