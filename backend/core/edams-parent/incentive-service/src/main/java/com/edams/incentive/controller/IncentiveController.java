package com.edams.incentive.controller;

import com.edams.incentive.entity.IncentiveRule;
import com.edams.incentive.entity.PointsRecord;
import com.edams.incentive.service.IncentiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/incentive")
@RequiredArgsConstructor
public class IncentiveController {

    private final IncentiveService incentiveService;

    @PostMapping("/rule")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody IncentiveRule rule) {
        IncentiveRule saved = incentiveService.createRule(rule);
        return ResponseEntity.ok(Map.of("code", 200, "message", "规则创建成功", "data", saved));
    }

    @GetMapping("/rule/list")
    public ResponseEntity<Map<String, Object>> listRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<IncentiveRule> rules = incentiveService.listRules(page * size, size);
        long total = incentiveService.countRules();
        return ResponseEntity.ok(Map.of("code", 200, "data", rules, "total", total));
    }

    @PutMapping("/rule/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(@PathVariable Long id, @RequestBody IncentiveRule rule) {
        rule.setId(id);
        incentiveService.updateRule(rule);
        return ResponseEntity.ok(Map.of("code", 200, "message", "规则更新成功"));
    }

    @DeleteMapping("/rule/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Long id) {
        incentiveService.deleteRule(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "规则删除成功"));
    }

    @PostMapping("/points/earn")
    public ResponseEntity<Map<String, Object>> earnPoints(@RequestBody Map<String, Object> req) {
        String userId = (String) req.get("userId");
        String action = (String) req.get("action");
        String targetId = (String) req.get("targetId");
        PointsRecord record = incentiveService.earnPoints(userId, action, targetId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "积分已发放", "data", record));
    }

    @GetMapping("/points/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPoints(@PathVariable String userId) {
        long total = incentiveService.getUserTotalPoints(userId);
        List<PointsRecord> records = incentiveService.getUserPointsHistory(userId, 0, 20);
        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("total", total, "records", records)));
    }

    @GetMapping("/rank")
    public ResponseEntity<Map<String, Object>> getRankList(@RequestParam(defaultValue = "10") int top) {
        List<Map<String, Object>> rank = incentiveService.getRankList(top);
        return ResponseEntity.ok(Map.of("code", 200, "data", rank));
    }
}
