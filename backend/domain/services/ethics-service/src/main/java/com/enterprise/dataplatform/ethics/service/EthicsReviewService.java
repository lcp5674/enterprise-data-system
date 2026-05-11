package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsReviewRequest;
import com.enterprise.dataplatform.ethics.domain.dto.request.ReviewCommentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsReviewResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsReview;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.domain.enums.ReviewStatus;
import com.enterprise.dataplatform.ethics.repository.EthicsReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EthicsReviewService {

    private final EthicsReviewRepository reviewRepository;

    @Transactional
    public EthicsReviewResponse createReview(EthicsReviewRequest request, String requester) {
        log.info("创建伦理审查: 资产={}, 申请人={}", request.getAssetId(), requester);

        String reviewCode = "ER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EthicsReview review = EthicsReview.builder()
                .reviewCode(reviewCode)
                .title(request.getTitle())
                .description(request.getDescription())
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .riskLevel(request.getRiskLevel() != null ?
                        EthicsLevel.valueOf(request.getRiskLevel()) : EthicsLevel.MEDIUM)
                .reviewType(request.getReviewType())
                .status(ReviewStatus.DRAFT)
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .requester(requester)
                .requesterDepartment(request.getRequesterDepartment())
                .justification(request.getJustification())
                .alternativeAnalysis(request.getAlternativeAnalysis())
                .stakeholderImpact(request.getStakeholderImpact())
                .mitigationPlan(request.getMitigationPlan())
                .supportingDocs(request.getSupportingDocs())
                .conditions(request.getConditions() != null ?
                        Arrays.asList(request.getConditions().split(",")) : new ArrayList<>())
                .reviewComments(new ArrayList<>())
                .version(1)
                .build();

        review = reviewRepository.save(review);

        log.info("伦理审查创建成功: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse submitReview(Long id, String submitter) {
        log.info("提交伦理审查: id={}, 提交人={}", id, submitter);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.DRAFT &&
                review.getStatus() != ReviewStatus.REVISION_REQUESTED) {
            throw new IllegalStateException("当前状态不允许提交: " + review.getStatus());
        }

        validateReviewSubmission(review);

        review.setStatus(ReviewStatus.PENDING);
        review.setSubmittedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        log.info("伦理审查提交成功: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse startReview(Long id, String reviewer) {
        log.info("开始审核伦理审查: id={}, 审核人={}", id, reviewer);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new IllegalStateException("当前状态不允许开始审核: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.IN_REVIEW);
        review.setReviewer(reviewer);
        review.setReviewedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        log.info("伦理审查开始审核: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse addReviewComment(Long id, ReviewCommentRequest request, String commenter) {
        log.info("添加审查意见: id={}, 评论人={}", id, commenter);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        List<String> comments = review.getReviewComments();
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add("[" + commenter + " - " + LocalDateTime.now() + "]: " + request.getComment());
        review.setReviewComments(comments);

        review = reviewRepository.save(review);

        log.info("审查意见添加成功: {}", id);
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse approveReview(Long id, String approver, String approvalComments,
            List<String> conditions) {
        log.info("批准伦理审查: id={}, 批准人={}", id, approver);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.IN_REVIEW) {
            throw new IllegalStateException("当前状态不允许批准: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.APPROVED);
        review.setApprover(approver);
        review.setApprovedAt(LocalDateTime.now());
        review.setApprovalComments(approvalComments);
        if (conditions != null) {
            review.setConditions(conditions);
        }
        review.setExpiryDate(LocalDateTime.now().plusYears(1));

        review = reviewRepository.save(review);

        log.info("伦理审查批准成功: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse rejectReview(Long id, String approver, String rejectionReason) {
        log.info("拒绝伦理审查: id={}, 审批人={}", id, approver);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.IN_REVIEW) {
            throw new IllegalStateException("当前状态不允许拒绝: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.REJECTED);
        review.setApprover(approver);
        review.setApprovedAt(LocalDateTime.now());
        review.setRejectionReason(rejectionReason);

        review = reviewRepository.save(review);

        log.info("伦理审查拒绝: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse requestRevision(Long id, String reviewer, String revisionReason) {
        log.info("要求修改伦理审查: id={}, 审核人={}", id, reviewer);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.IN_REVIEW) {
            throw new IllegalStateException("当前状态不允许要求修改: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.REVISION_REQUESTED);
        List<String> comments = review.getReviewComments();
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add("[SYSTEM - " + LocalDateTime.now() + "]: 要求修改: " + revisionReason);
        review.setReviewComments(comments);

        review = reviewRepository.save(review);

        log.info("伦理审查要求修改: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse executeReview(Long id, String executor) {
        log.info("执行伦理审查: id={}, 执行人={}", id, executor);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() != ReviewStatus.APPROVED) {
            throw new IllegalStateException("当前状态不允许执行: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.EXECUTED);
        review.setExecutedAt(LocalDateTime.now());

        review = reviewRepository.save(review);

        log.info("伦理审查执行完成: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    @Transactional
    public EthicsReviewResponse cancelReview(Long id, String canceller) {
        log.info("取消伦理审查: id={}, 操作人={}", id, canceller);

        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));

        if (review.getStatus() == ReviewStatus.EXECUTED) {
            throw new IllegalStateException("已执行的审查不能取消");
        }

        review.setStatus(ReviewStatus.CANCELLED);

        review = reviewRepository.save(review);

        log.info("伦理审查已取消: {}", review.getReviewCode());
        return EthicsReviewResponse.fromEntity(review);
    }

    public EthicsReviewResponse getReview(Long id) {
        EthicsReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + id));
        return EthicsReviewResponse.fromEntity(review);
    }

    public EthicsReviewResponse getReviewByCode(String reviewCode) {
        EthicsReview review = reviewRepository.findByReviewCode(reviewCode)
                .orElseThrow(() -> new IllegalArgumentException("审查不存在: " + reviewCode));
        return EthicsReviewResponse.fromEntity(review);
    }

    public Page<EthicsReviewResponse> searchReviews(
            String status, String reviewType, String priority, String assetId, Pageable pageable) {
        ReviewStatus reviewStatus = status != null ? ReviewStatus.valueOf(status) : null;
        return reviewRepository.searchReviews(reviewStatus, reviewType, priority, assetId, pageable)
                .map(EthicsReviewResponse::fromEntity);
    }

    public List<EthicsReviewResponse> getPendingReviews(String reviewer) {
        List<ReviewStatus> pendingStatuses = Arrays.asList(
                ReviewStatus.PENDING, ReviewStatus.IN_REVIEW);
        return reviewRepository.findPendingReviewsByReviewer(reviewer, pendingStatuses)
                .stream()
                .map(EthicsReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processExpiredReviews() {
        log.info("处理过期审查...");

        List<EthicsReview> expiredReviews = reviewRepository.findExpiredReviews(
                LocalDateTime.now(), ReviewStatus.APPROVED);

        for (EthicsReview review : expiredReviews) {
            review.setStatus(ReviewStatus.EXPIRED);
            reviewRepository.save(review);
            log.info("审查已过期: {}", review.getReviewCode());
        }
    }

    private void validateReviewSubmission(EthicsReview review) {
        List<String> errors = new ArrayList<>();

        if (review.getTitle() == null || review.getTitle().isEmpty()) {
            errors.add("标题不能为空");
        }
        if (review.getAssetId() == null || review.getAssetId().isEmpty()) {
            errors.add("资产ID不能为空");
        }
        if (review.getReviewType() == null || review.getReviewType().isEmpty()) {
            errors.add("审查类型不能为空");
        }
        if (review.getJustification() == null || review.getJustification().isEmpty()) {
            errors.add("理由说明不能为空");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("审查提交验证失败: " + String.join(", ", errors));
        }
    }
}
