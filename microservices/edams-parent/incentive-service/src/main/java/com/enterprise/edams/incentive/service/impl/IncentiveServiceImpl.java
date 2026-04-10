package com.enterprise.edams.incentive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.incentive.dto.*;
import com.enterprise.edams.incentive.entity.*;
import com.enterprise.edams.incentive.mapper.PointTransactionMapper;
import com.enterprise.edams.incentive.mapper.UserPointsMapper;
import com.enterprise.edams.incentive.service.IncentiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 激励服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class IncentiveServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints>
        implements IncentiveService {
    
    private final PointTransactionMapper transactionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LEADERBOARD_KEY = "leaderboard:points:";
    private static final String USER_RANK_KEY = "user:rank:";
    
    @Override
    public UserPoints getUserPoints(Long userId) {
        UserPoints userPoints = this.getOne(new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId));
        
        if (userPoints == null) {
            userPoints = createUserPoints(userId);
        }
        
        return userPoints;
    }
    
    @Override
    public PointTransaction awardPoints(Long userId, BizType bizType, String bizId, String description) {
        // 获取奖励规则
        BigDecimal points = getRewardPoints(bizType);
        
        UserPoints userPoints = getUserPoints(userId);
        userPoints.setBalance(userPoints.getBalance().add(points));
        userPoints.setTotalEarned(userPoints.getTotalEarned().add(points));
        userPoints.setExperience(userPoints.getExperience().add(points));
        updateUserLevel(userPoints);
        this.updateById(userPoints);
        
        // 记录交易
        PointTransaction transaction = PointTransaction.builder()
                .transactionNo(generateTransactionNo())
                .userId(userId)
                .type(TransactionType.EARN)
                .points(points)
                .balanceAfter(userPoints.getBalance())
                .bizType(bizType)
                .bizId(bizId)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionMapper.insert(transaction);
        
        // 更新Redis排行
        updateLeaderboard(userId, userPoints.getBalance());
        
        log.info("积分发放成功: userId={}, bizType={}, points={}", userId, bizType, points);
        return transaction;
    }
    
    @Override
    public PointTransaction deductPoints(Long userId, BigDecimal points, String description) {
        UserPoints userPoints = getUserPoints(userId);
        
        if (userPoints.getBalance().compareTo(points) < 0) {
            throw new RuntimeException("积分余额不足");
        }
        
        userPoints.setBalance(userPoints.getBalance().subtract(points));
        userPoints.setTotalSpent(userPoints.getTotalSpent().add(points));
        this.updateById(userPoints);
        
        PointTransaction transaction = PointTransaction.builder()
                .transactionNo(generateTransactionNo())
                .userId(userId)
                .type(TransactionType.SPEND)
                .points(points)
                .balanceAfter(userPoints.getBalance())
                .bizType(BizType.EXCHANGE)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionMapper.insert(transaction);
        
        updateLeaderboard(userId, userPoints.getBalance());
        
        return transaction;
    }
    
    @Override
    public Page<PointTransaction> getTransactionHistory(Long userId, TransactionSearchRequest request) {
        Page<PointTransaction> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<PointTransaction> wrapper = new LambdaQueryWrapper<PointTransaction>()
                .eq(PointTransaction::getUserId, userId);
        
        if (request.getType() != null) {
            wrapper.eq(PointTransaction::getType, request.getType());
        }
        if (request.getBizType() != null) {
            wrapper.eq(PointTransaction::getBizType, request.getBizType());
        }
        
        wrapper.orderByDesc(PointTransaction::getCreatedTime);
        return transactionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public SignInResult signIn(Long userId) {
        UserPoints userPoints = getUserPoints(userId);
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否已签到
        if (userPoints.getLastSignIn() != null) {
            LocalDateTime lastSign = userPoints.getLastSignIn();
            if (lastSign.toLocalDate().equals(now.toLocalDate())) {
                return SignInResult.builder()
                        .success(false)
                        .message("今日已签到")
                        .points(BigDecimal.ZERO)
                        .totalDays(userPoints.getConsecutiveDays())
                        .build();
            }
            
            // 检查连续签到
            if (lastSign.toLocalDate().plusDays(1).equals(now.toLocalDate())) {
                userPoints.setConsecutiveDays(userPoints.getConsecutiveDays() + 1);
            } else {
                userPoints.setConsecutiveDays(1);
            }
        } else {
            userPoints.setConsecutiveDays(1);
        }
        
        userPoints.setLastSignIn(now);
        
        // 签到积分（连签加成）
        BigDecimal basePoints = BigDecimal.valueOf(10);
        BigDecimal bonusPoints = BigDecimal.valueOf(userPoints.getConsecutiveDays() > 7 ? 5 : 0);
        BigDecimal totalPoints = basePoints.add(bonusPoints);
        
        userPoints.setBalance(userPoints.getBalance().add(totalPoints));
        userPoints.setTotalEarned(userPoints.getTotalEarned().add(totalPoints));
        userPoints.setExperience(userPoints.getExperience().add(totalPoints));
        updateUserLevel(userPoints);
        this.updateById(userPoints);
        
        // 记录交易
        PointTransaction transaction = PointTransaction.builder()
                .transactionNo(generateTransactionNo())
                .userId(userId)
                .type(TransactionType.EARN)
                .points(totalPoints)
                .balanceAfter(userPoints.getBalance())
                .bizType(BizType.SIGN_IN)
                .description("签到获得积分，连续签到" + userPoints.getConsecutiveDays() + "天")
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionMapper.insert(transaction);
        
        updateLeaderboard(userId, userPoints.getBalance());
        
        return SignInResult.builder()
                .success(true)
                .message("签到成功")
                .points(totalPoints)
                .bonusPoints(bonusPoints)
                .totalDays(userPoints.getConsecutiveDays())
                .balance(userPoints.getBalance())
                .build();
    }
    
    @Override
    public List<LeaderboardEntry> getLeaderboard(String period, int limit) {
        String key = LEADERBOARD_KEY + period;
        
        // 从Redis获取排行
        Set<Object> topUsers = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (topUsers == null || topUsers.isEmpty()) {
            // 从数据库获取
            Page<UserPoints> page = new Page<>(1, limit);
            Page<UserPoints> result = this.page(page, new LambdaQueryWrapper<UserPoints>()
                    .orderByDesc(UserPoints::getBalance)
                    .select(UserPoints::getUserId, UserPoints::getUserName, UserPoints::getBalance));
            
            return result.getRecords().stream()
                    .map(u -> LeaderboardEntry.builder()
                            .rank(0)
                            .userId(u.getUserId())
                            .userName(u.getUserName())
                            .points(u.getBalance())
                            .build())
                    .collect(Collectors.toList());
        }
        
        List<LeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Object userId : topUsers) {
            UserPoints user = this.getOne(new LambdaQueryWrapper<UserPoints>()
                    .eq(UserPoints::getUserId, ((Number) userId).longValue()));
            if (user != null) {
                entries.add(LeaderboardEntry.builder()
                        .rank(rank++)
                        .userId(user.getUserId())
                        .userName(user.getUserName())
                        .points(user.getBalance())
                        .build());
            }
        }
        return entries;
    }
    
    @Override
    public UserRank getUserRank(Long userId, String period) {
        String key = LEADERBOARD_KEY + period;
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId);
        
        UserPoints userPoints = getUserPoints(userId);
        Long total = redisTemplate.opsForZSet().zCard(key);
        
        return UserRank.builder()
                .userId(userId)
                .userName(userPoints.getUserName())
                .rank(rank != null ? rank.intValue() + 1 : null)
                .points(userPoints.getBalance())
                .totalParticipants(total != null ? total.intValue() : 0)
                .level(userPoints.getLevel())
                .build();
    }
    
    @Override
    public PointsStatistics getStatistics() {
        long totalUsers = this.count();
        BigDecimal totalPoints = this.list().stream()
                .map(UserPoints::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgPoints = totalUsers > 0 ? 
                totalPoints.divide(BigDecimal.valueOf(totalUsers), 2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        return PointsStatistics.builder()
                .totalUsers(totalUsers)
                .totalPoints(totalPoints)
                .averagePoints(avgPoints)
                .build();
    }
    
    @Override
    public List<PointTransaction> batchAward(Long userId, List<BizType> bizTypes) {
        List<PointTransaction> results = new ArrayList<>();
        for (BizType bizType : bizTypes) {
            try {
                PointTransaction transaction = awardPoints(userId, bizType, null, null);
                results.add(transaction);
            } catch (Exception e) {
                log.error("批量发放积分失败: userId={}, bizType={}", userId, bizType, e);
            }
        }
        return results;
    }
    
    // ============ 私有方法 ============
    
    private UserPoints createUserPoints(Long userId) {
        UserPoints userPoints = UserPoints.builder()
                .userId(userId)
                .userName("用户" + userId)
                .balance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .totalSpent(BigDecimal.ZERO)
                .level(1)
                .experience(BigDecimal.ZERO)
                .title("新手")
                .consecutiveDays(0)
                .build();
        this.save(userPoints);
        return userPoints;
    }
    
    private BigDecimal getRewardPoints(BizType bizType) {
        // 根据业务类型获取奖励积分
        Map<BizType, BigDecimal> rewardMap = Map.of(
                BizType.DATA_QUALITY, BigDecimal.valueOf(20),
                BizType.LINEAGE_COMPLETE, BigDecimal.valueOf(15),
                BizType.METADATA_COMPLETE, BigDecimal.valueOf(10),
                BizType.TASK_COMPLETE, BigDecimal.valueOf(5),
                BizType.REVIEW_PASS, BigDecimal.valueOf(10),
                BizType.CONTRIBUTION, BigDecimal.valueOf(30),
                BizType.SIGN_IN, BigDecimal.valueOf(10)
        );
        return rewardMap.getOrDefault(bizType, BigDecimal.valueOf(5));
    }
    
    private void updateUserLevel(UserPoints userPoints) {
        BigDecimal exp = userPoints.getExperience();
        int level;
        String title;
        
        if (exp.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            level = 10;
            title = "数据大师";
        } else if (exp.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            level = 8;
            title = "资深专家";
        } else if (exp.compareTo(BigDecimal.valueOf(2000)) >= 0) {
            level = 6;
            title = "专家";
        } else if (exp.compareTo(BigDecimal.valueOf(500)) >= 0) {
            level = 4;
            title = "熟手";
        } else {
            level = 2;
            title = "新手";
        }
        
        userPoints.setLevel(level);
        userPoints.setTitle(title);
    }
    
    private void updateLeaderboard(Long userId, BigDecimal points) {
        String key = LEADERBOARD_KEY + "all";
        redisTemplate.opsForZSet().add(key, userId.toString(), points.doubleValue());
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }
    
    private String generateTransactionNo() {
        return "TXN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
