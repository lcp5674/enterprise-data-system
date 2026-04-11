package com.enterprise.edams.collaboration.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.collaboration.entity.Mention;
import com.enterprise.edams.collaboration.service.MentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 提及控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/collaboration/mention")
@RequiredArgsConstructor
@Tag(name = "提及管理", description = "提及管理接口")
public class MentionController {

    private final MentionService mentionService;

    @PostMapping
    @Operation(summary = "创建提及", description = "创建新的提及")
    public Result<Mention> createMention(@RequestBody Mention mention) {
        Mention created = mentionService.createMention(mention);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新提及", description = "更新指定的提及")
    public Result<Mention> updateMention(@PathVariable Long id, @RequestBody Mention mention) {
        Mention updated = mentionService.updateMention(id, mention);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取提及", description = "根据ID获取提及")
    public Result<Mention> getMention(@PathVariable Long id) {
        Mention mention = mentionService.getMention(id);
        return Result.success(mention);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除提及", description = "删除指定的提及")
    public Result<Void> deleteMention(@PathVariable Long id) {
        mentionService.deleteMention(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询提及", description = "分页查询所有提及")
    public Result<IPage<Mention>> listMentions(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Mention> page = mentionService.listMentions(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "根据用户ID和状态查询提及", description = "根据用户ID和状态查询提及")
    public Result<IPage<Mention>> listMentionsByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Mention> page = mentionService.listMentionsByUserIdAndStatus(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/source/{sourceId}/{sourceType}")
    @Operation(summary = "根据来源查询提及", description = "根据来源查询提及")
    public Result<IPage<Mention>> listMentionsBySource(
            @PathVariable Long sourceId,
            @PathVariable Integer sourceType,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Mention> page = mentionService.listMentionsBySource(sourceId, sourceType, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/mentioner/{mentionerId}")
    @Operation(summary = "根据提及者ID查询提及", description = "根据提及者ID查询提及")
    public Result<IPage<Mention>> listMentionsByMentionerId(
            @PathVariable Long mentionerId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Mention> page = mentionService.listMentionsByMentionerId(mentionerId, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记提及为已读", description = "标记提及为已读状态")
    public Result<Mention> markMentionAsRead(@PathVariable Long id) {
        Mention mention = mentionService.markMentionAsRead(id);
        return Result.success(mention);
    }

    @PostMapping("/{id}/handled")
    @Operation(summary = "标记提及为已处理", description = "标记提及为已处理状态")
    public Result<Mention> markMentionAsHandled(@PathVariable Long id) {
        Mention mention = mentionService.markMentionAsHandled(id);
        return Result.success(mention);
    }

    @PostMapping("/user/{userId}/batchRead")
    @Operation(summary = "批量标记提及为已读", description = "批量标记用户的提及为已读")
    public Result<Void> batchMarkMentionsAsRead(@PathVariable Long userId) {
        mentionService.batchMarkMentionsAsRead(userId);
        return Result.success();
    }
}