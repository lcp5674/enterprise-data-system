package com.edams.incentive.service;

import com.edams.incentive.entity.IncentiveRule;
import com.edams.incentive.entity.PointsRecord;
import java.util.List;
import java.util.Map;

public interface IncentiveService {
    IncentiveRule createRule(IncentiveRule rule);
    List<IncentiveRule> listRules(int offset, int size);
    long countRules();
    void updateRule(IncentiveRule rule);
    void deleteRule(Long id);
    PointsRecord earnPoints(String userId, String action, String targetId);
    long getUserTotalPoints(String userId);
    List<PointsRecord> getUserPointsHistory(String userId, int offset, int size);
    List<Map<String, Object>> getRankList(int top);
}
