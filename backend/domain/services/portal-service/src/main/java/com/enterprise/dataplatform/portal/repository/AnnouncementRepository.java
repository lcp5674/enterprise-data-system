package com.enterprise.dataplatform.portal.repository;

import com.enterprise.dataplatform.portal.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Announcement Repository
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByStatusOrderByPriorityDesc(Announcement.AnnouncementStatus status);

    List<Announcement> findByStatusAndExpiredAtAfterOrderByPriorityDesc(
            Announcement.AnnouncementStatus status, LocalDateTime now);

    List<Announcement> findByPublishedByOrderByPublishedAtDesc(String publishedBy);
}
