package com.enterprise.dataplatform.portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Announcement Entity
 * 系统公告
 */
@Data
@Entity
@Table(name = "portal_announcement")
public class Announcement {

    public enum AnnouncementType {
        INFO,      // 信息公告
        WARNING,   // 警告公告
        ALERT      // 紧急公告
    }

    public enum AnnouncementStatus {
        DRAFT,     // 草稿
        PUBLISHED, // 已发布
        EXPIRED    // 已过期
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementType type = AnnouncementType.INFO;

    private Integer priority = 0;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "target_roles", columnDefinition = "TEXT")
    private String targetRoles; // JSON array of role ids
}
