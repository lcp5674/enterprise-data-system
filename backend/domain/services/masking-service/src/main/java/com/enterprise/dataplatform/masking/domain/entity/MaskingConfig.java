package com.enterprise.dataplatform.masking.domain.entity;

import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "masking_config", indexes = {
    @Index(name = "idx_config_name", columnList = "configName"),
    @Index(name = "idx_masking_type", columnList = "maskingType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", nullable = false, length = 128)
    private String configName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "masking_type", nullable = false, length = 32)
    private MaskingType maskingType;

    @Column(name = "custom_pattern", length = 256)
    private String customPattern;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "preserve_format")
    private Boolean preserveFormat;

    @Column(name = "mask_char", length = 4)
    private String maskChar;

    @Column(name = "show_first_n")
    private Integer showFirstN;

    @Column(name = "show_last_n")
    private Integer showLastN;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
