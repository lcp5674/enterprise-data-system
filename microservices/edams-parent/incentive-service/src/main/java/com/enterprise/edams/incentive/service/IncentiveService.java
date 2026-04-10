package com.enterprise.edams.incentive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.incentive.dto.*;
import com.enterprise.edams.incentive.entity.*;
import java.util.List;
import java.util.Map;

/**
 * 激励服务接口
 */
public interface IncentiveService extends IService<UserPoints> {
    
    /**
     * 获取用户积分
     */
    UserPoints getUserPoints(Long userId);
    
    /**
     * 授予积分
     */
    PointTransaction awardPoints(Long userId, BizType bizType, String bizId, String description);
    
    /**
     * 消耗积分
     */
    PointTransaction deductPoints(Long userId, java.math.BigDecimal points, String description);
    
    /**
     * 获取积分明细
     */
    Page<PointTransaction> getTransactionHistory(Long userId, TransactionSearchRequest request);
    
    /**
     * 签到
     */
    SignInResult signIn(Long userId);
    
    /**
     * 获取排行榜
     */
    List<LeaderboardEntry> getLeaderboard(String period, int limit);
    
    /**
     * 获取用户排名
     */
    UserRank getUserRank(Long userId, String period);
    
    /**
     * 获取积分统计
     */
    PointsStatistics getStatistics();
    
    /**
     * 批量发放积分
     */
    List<PointTransaction> batchAward(Long userId, List<BizType> bizTypes);
}
