package com.enterprise.edams.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.auth.entity.LoginLog;
import com.enterprise.edams.auth.repository.LoginLogMapper;
import com.enterprise.edams.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 登录日志控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth/logs")
@RequiredArgsConstructor
@Tag(name = "登录日志", description = "用户登录日志查询接口")
public class LoginLogController {

    private final LoginLogMapper loginLogMapper;

    /**
     * 分页查询登录日志
     */
    @GetMapping
    @Operation(summary = "分页查询登录日志", description = "支持按用户名、状态、时间范围等条件筛选")
    public PageResult<LoginLog> queryLoginLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "登录状态：0-失败，1-成功") @RequestParam(required = false) Integer status,
            @Parameter(description = "IP地址") @RequestParam(required = false) String ip,
            @Parameter(description = "开始时间") @RequestParam(required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(LoginLog::getUserId, userId);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(LoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(LoginLog::getStatus, status);
        }
        if (ip != null && !ip.isEmpty()) {
            wrapper.like(LoginLog::getIp, ip);
        }
        if (startTime != null) {
            wrapper.ge(LoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(LoginLog::getLoginTime, endTime);
        }

        wrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> resultPage = loginLogMapper.selectPage(page, wrapper);

        return PageResult.success(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize());
    }

    /**
     * 查询用户最近登录记录
     */
    @GetMapping("/user/{userId}/recent")
    @Operation(summary = "查询用户最近登录记录", description = "获取指定用户最近N条登录日志")
    public Object getRecentUserLogs(
            @PathVariable Long userId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {

        return loginLogMapper.findRecentByUserId(userId, Math.min(limit, 50));
    }

    /**
     * 获取今日登录统计
     */
    @GetMapping("/stats/today")
    @Operation(summary = "今日登录统计", description = "获取今日登录成功/失败次数、活跃用户数等统计信息")
    public Result<Object> getTodayStats() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // 使用MyBatis Plus的LambdaQueryWrapper进行统计查询
        long successCount = loginLogMapper.selectCount(
                new LambdaQueryWrapper<LoginLog>()
                        .eq(LoginLog::getStatus, 1)
                        .ge(LoginLog::getLoginTime, startOfDay)
                        .le(LoginLog::getLoginTime, endOfDay));

        long failCount = loginLogMapper.selectCount(
                new LambdaQueryWrapper<LoginLog>()
                        .eq(LoginLog::getStatus, 0)
                        .ge(LoginLog::getLoginTime, startOfDay)
                        .le(LoginLog::getLoginTime, endOfDay));

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("totalCount", successCount + failCount);
        stats.put("date", java.time.LocalDate.now().toString());

        return Result.success(stats);
    }
}
