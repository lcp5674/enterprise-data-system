package com.edams.incentive.service.impl;

import com.edams.incentive.entity.IncentiveRule;
import com.edams.incentive.entity.PointsRecord;
import com.edams.incentive.repository.IncentiveMapper;
import com.edams.incentive.service.IncentiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncentiveServiceImpl implements IncentiveService {

    private final IncentiveMapper incentiveMapper;

    @Override
    public IncentiveRule createRule(IncentiveRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setStatus("ACTIVE");
        incentiveMapper.insertRule(rule);
        return rule;
    }

    @Override
    public List<IncentiveRule> listRules(int offset, int size) {
        return incentiveMapper.findAllRules(offset, size);
    }

    @Override
    public long countRules() {
        return incentiveMapper.countRules();
    }

    @Override
    public void updateRule(IncentiveRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        incentiveMapper.updateRule(rule);
    }

    @Override
    public void deleteRule(Long id) {
        incentiveMapper.deleteRule(id);
    }

    @Override
    public PointsRecord earnPoints(String userId, String action, String targetId) {
        IncentiveRule rule = incentiveMapper.findRuleByAction(action);
        int points = (rule != null) ? rule.getPoints() : 10;
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setAction(action);
        record.setTargetId(targetId);
        record.setPoints(points);
        record.setCreateTime(LocalDateTime.now());
        incentiveMapper.insertPointsRecord(record);
        log.info("Points earned: userId={}, action={}, points={}", userId, action, points);
        return record;
    }

    @Override
    public long getUserTotalPoints(String userId) {
        return incentiveMapper.sumUserPoints(userId);
    }

    @Override
    public List<PointsRecord> getUserPointsHistory(String userId, int offset, int size) {
        return incentiveMapper.findUserPoints(userId, offset, size);
    }

    @Override
    public List<Map<String, Object>> getRankList(int top) {
        return incentiveMapper.getRankList(top);
    }
}
