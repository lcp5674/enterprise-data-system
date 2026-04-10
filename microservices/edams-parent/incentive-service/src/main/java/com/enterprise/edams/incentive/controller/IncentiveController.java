package com.enterprise.edams.incentive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.incentive.dto.*;
import com.enterprise.edams.incentive.entity.*;
import com.enterprise.edams.incentive.service.IncentiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/incentive")
@Tag(name = "积分激励", description = "积分激励与排行榜接口")
public class IncentiveController {
    
    private final IncentiveService incentiveService;
    
    @GetMapping("/points/{userId}")
    @Operation(summary = "获取用户积分", description = "获取用户积分信息")
    public UserPoints getUserPoints(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return incentiveService.getUserPoints(userId);
    }
    
    @PostMapping("/points/award")
    @Operation(summary = "发放积分", description = "发放积分给用户")
    public PointTransaction awardPoints(
            @RequestParam Long userId,
            @RequestParam BizType bizType,
            @RequestParam(required = false) String bizId,
            @RequestParam(required = false) String description) {
        return incentiveService.awardPoints(userId, bizType, bizId, description);
    }
    
    @PostMapping("/points/deduct")
    @Operation(summary = "扣除积分", description = "扣除用户积分")
    public PointTransaction deductPoints(
            @RequestParam Long userId,
            @RequestParam BigDecimal points,
            @RequestParam String description) {
        return incentiveService.deductPoints(userId, points, description);
    }
    
    @GetMapping("/transactions/{userId}")
    @Operation(summary = "积分明细", description = "获取用户积分明细")
    public Page<PointTransaction> getTransactions(
            @PathVariable Long userId,
            TransactionSearchRequest request) {
        return incentiveService.getTransactionHistory(userId, request);
    }
    
    @PostMapping("/sign-in/{userId}")
    @Operation(summary = "签到", description = "用户签到")
    public SignInResult signIn(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return incentiveService.signIn(userId);
    }
    
    @GetMapping("/leaderboard")
    @Operation(summary = "排行榜", description = "获取积分排行榜")
    public List<LeaderboardEntry> getLeaderboard(
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(defaultValue = "10") int limit) {
        return incentiveService.getLeaderboard(period, limit);
    }
    
    @GetMapping("/rank/{userId}")
    @Operation(summary = "用户排名", description = "获取用户排名")
    public UserRank getUserRank(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "all") String period) {
        return incentiveService.getUserRank(userId, period);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "积分统计", description = "获取积分统计信息")
    public PointsStatistics getStatistics() {
        return incentiveService.getStatistics();
    }
}
