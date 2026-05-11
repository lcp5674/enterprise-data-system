package com.enterprise.dataplatform.ethics.domain.entity;

import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsScore;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ethics_assessment", indexes = {
    @Index(name = "idx_assessment_asset", columnList = "assetId"),
    @Index(name = "idx_assessment_framework", columnList = "frameworkId"),
    @Index(name = "idx_assessment_risk", columnList = "overallRisk"),
    @Index(name = "idx_assessment_time", columnList = "assessedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assessment_code", nullable = false, unique = true, length = 64)
    private String assessmentCode;

    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    @Column(name = "asset_name", length = 256)
    private String assetName;

    @Column(name = "framework_id")
    private Long frameworkId;

    @Column(name = "framework_name", length = 128)
    private String frameworkName;

    @Enumerated(EnumType.STRING)
    @Column(name = "transparency_score", length = 32)
    private EthicsScore transparencyScore;

    @Column(name = "transparency_value")
    private BigDecimal transparencyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "fairness_score", length = 32)
    private EthicsScore fairnessScore;

    @Column(name = "fairness_value")
    private BigDecimal fairnessValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountability_score", length = 32)
    private EthicsScore accountabilityScore;

    @Column(name = "accountability_value")
    private BigDecimal accountabilityValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_score", length = 32)
    private EthicsScore privacyScore;

    @Column(name = "privacy_value")
    private BigDecimal privacyValue;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk", length = 32)
    private RiskLevel overallRisk;

    @Column(name = "recommendations", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @Column(name = "concerns", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> concerns = new ArrayList<>();

    @Column(name = "methodology", length = 32)
    @Builder.Default
    private String methodology = "AUTOMATED";

    @Column(name = "assessor", length = 64)
    private String assessor;

    @CreationTimestamp
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", length = 32)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
