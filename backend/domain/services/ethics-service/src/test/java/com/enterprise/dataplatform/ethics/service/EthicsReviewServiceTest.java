package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsReviewRequest;
import com.enterprise.dataplatform.ethics.domain.dto.request.ReviewCommentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsReviewResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsReview;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.domain.enums.ReviewStatus;
import com.enterprise.dataplatform.ethics.repository.EthicsReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EthicsReviewServiceTest {

    @Mock
    private EthicsReviewRepository reviewRepository;

    @InjectMocks
    private EthicsReviewService reviewService;

    private EthicsReviewRequest request;
    private EthicsReview review;

    @BeforeEach
    void setUp() {
        request = EthicsReviewRequest.builder()
                .title("数据使用伦理审查申请")
                .description("申请使用客户数据进行营销分析")
                .assetId("ASSET-001")
                .assetName("客户数据库")
                .riskLevel("MEDIUM")
                .reviewType("DATA_USAGE")
                .priority("HIGH")
                .requesterDepartment("市场部")
                .justification("用于改进营销策略，提升客户体验")
                .alternativeAnalysis("已考虑匿名化处理方案")
                .stakeholderImpact("对客户隐私有一定影响，需加强保护措施")
                .mitigationPlan("实施数据脱敏，限制访问权限")
                .build();

        review = EthicsReview.builder()
                .id(1L)
                .reviewCode("ER-TEST001")
                .title("数据使用伦理审查申请")
                .description("申请使用客户数据进行营销分析")
                .assetId("ASSET-001")
                .assetName("客户数据库")
                .riskLevel(EthicsLevel.MEDIUM)
                .reviewType("DATA_USAGE")
                .status(ReviewStatus.DRAFT)
                .priority("HIGH")
                .requester("admin")
                .requesterDepartment("市场部")
                .justification("用于改进营销策略")
                .version(1)
                .build();
    }

    @Test
    void testCreateReview_Success() {
        when(reviewRepository.save(any(EthicsReview.class))).thenReturn(review);

        EthicsReviewResponse response = reviewService.createReview(request, "admin");

        assertNotNull(response);
        assertEquals("ER-TEST001", response.getReviewCode());
        assertEquals("数据使用伦理审查申请", response.getTitle());
        assertEquals("DRAFT", response.getStatus());

        verify(reviewRepository).save(any(EthicsReview.class));
    }

    @Test
    void testSubmitReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.PENDING);
            return saved;
        });

        EthicsReviewResponse response = reviewService.submitReview(1L, "admin");

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getSubmittedAt());
    }

    @Test
    void testSubmitReview_InvalidStatus() {
        review.setStatus(ReviewStatus.APPROVED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () ->
                reviewService.submitReview(1L, "admin"));
    }

    @Test
    void testStartReview_Success() {
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.IN_REVIEW);
            return saved;
        });

        EthicsReviewResponse response = reviewService.startReview(1L, "reviewer");

        assertNotNull(response);
        assertEquals("IN_REVIEW", response.getStatus());
        assertEquals("reviewer", response.getReviewer());
    }

    @Test
    void testApproveReview_Success() {
        review.setStatus(ReviewStatus.IN_REVIEW);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.APPROVED);
            return saved;
        });

        EthicsReviewResponse response = reviewService.approveReview(
                1L, "approver", "批准理由", Arrays.asList("条件1", "条件2"));

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertEquals("approver", response.getApprover());
        assertNotNull(response.getApprovedAt());
        assertNotNull(response.getExpiryDate());
    }

    @Test
    void testRejectReview_Success() {
        review.setStatus(ReviewStatus.IN_REVIEW);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.REJECTED);
            return saved;
        });

        EthicsReviewResponse response = reviewService.rejectReview(
                1L, "approver", "理由不充分");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("理由不充分", response.getRejectionReason());
    }

    @Test
    void testAddReviewComment_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenReturn(review);

        ReviewCommentRequest commentRequest = ReviewCommentRequest.builder()
                .comment("初步审核意见：需要补充更多细节")
                .build();

        EthicsReviewResponse response = reviewService.addReviewComment(
                1L, commentRequest, "reviewer");

        assertNotNull(response);
        verify(reviewRepository).save(any(EthicsReview.class));
    }

    @Test
    void testExecuteReview_Success() {
        review.setStatus(ReviewStatus.APPROVED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.EXECUTED);
            return saved;
        });

        EthicsReviewResponse response = reviewService.executeReview(1L, "executor");

        assertNotNull(response);
        assertEquals("EXECUTED", response.getStatus());
        assertNotNull(response.getExecutedAt());
    }

    @Test
    void testExecuteReview_InvalidStatus() {
        review.setStatus(ReviewStatus.PENDING);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () ->
                reviewService.executeReview(1L, "executor"));
    }

    @Test
    void testCancelReview_Success() {
        review.setStatus(ReviewStatus.DRAFT);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(EthicsReview.class))).thenAnswer(invocation -> {
            EthicsReview saved = invocation.getArgument(0);
            saved.setStatus(ReviewStatus.CANCELLED);
            return saved;
        });

        EthicsReviewResponse response = reviewService.cancelReview(1L, "admin");

        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void testCancelReview_ExecutedCannotCancel() {
        review.setStatus(ReviewStatus.EXECUTED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(IllegalStateException.class, () ->
                reviewService.cancelReview(1L, "admin"));
    }

    @Test
    void testGetReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        EthicsReviewResponse response = reviewService.getReview(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ER-TEST001", response.getReviewCode());
    }

    @Test
    void testGetReview_NotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.getReview(999L));
    }
}
