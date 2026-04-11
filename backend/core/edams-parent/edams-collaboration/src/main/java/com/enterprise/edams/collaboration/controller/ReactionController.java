package com.enterprise.edams.collaboration.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.collaboration.entity.Reaction;
import com.enterprise.edams.collaboration.service.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 反应控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/collaboration/reaction")
@RequiredArgsConstructor
@Tag(name = "反应管理", description = "反应管理接口")
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    @Operation(summary = "创建反应", description = "创建新的反应")
    public Result<Reaction> createReaction(@RequestBody Reaction reaction) {
        Reaction created = reactionService.createReaction(reaction);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新反应", description = "更新指定的反应")
    public Result<Reaction> updateReaction(@PathVariable Long id, @RequestBody Reaction reaction) {
        Reaction updated = reactionService.updateReaction(id, reaction);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取反应", description = "根据ID获取反应")
    public Result<Reaction> getReaction(@PathVariable Long id) {
        Reaction reaction = reactionService.getReaction(id);
        return Result.success(reaction);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除反应", description = "删除指定的反应")
    public Result<Void> deleteReaction(@PathVariable Long id) {
        reactionService.deleteReaction(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询反应", description = "分页查询所有反应")
    public Result<IPage<Reaction>> listReactions(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Reaction> page = reactionService.listReactions(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/comment/{commentId}")
    @Operation(summary = "根据评论ID查询反应", description = "根据评论ID查询相关的反应")
    public Result<IPage<Reaction>> listReactionsByCommentId(
            @PathVariable Long commentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Reaction> page = reactionService.listReactionsByCommentId(commentId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询反应", description = "根据用户ID查询该用户的所有反应")
    public Result<IPage<Reaction>> listReactionsByUserId(
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Reaction> page = reactionService.listReactionsByUserId(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/comment/{commentId}/type/{reactionType}")
    @Operation(summary = "根据评论ID和反应类型查询反应", description = "根据评论ID和反应类型查询相关的反应")
    public Result<IPage<Reaction>> listReactionsByCommentIdAndType(
            @PathVariable Long commentId,
            @PathVariable Integer reactionType,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Reaction> page = reactionService.listReactionsByCommentIdAndType(commentId, reactionType, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/count/{commentId}/{reactionType}")
    @Operation(summary = "统计反应数量", description = "统计评论相关的反应数量")
    public Result<Integer> countReactionsByCommentIdAndType(@PathVariable Long commentId, @PathVariable Integer reactionType) {
        Integer count = reactionService.countReactionsByCommentIdAndType(commentId, reactionType);
        return Result.success(count);
    }
}