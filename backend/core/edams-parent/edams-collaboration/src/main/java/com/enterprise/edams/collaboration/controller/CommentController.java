package com.enterprise.edams.collaboration.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.collaboration.entity.Comment;
import com.enterprise.edams.collaboration.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/collaboration/comment")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论管理接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "创建评论", description = "创建新的评论")
    public Result<Comment> createComment(@RequestBody Comment comment) {
        Comment created = commentService.createComment(comment);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评论", description = "更新指定的评论")
    public Result<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        Comment updated = commentService.updateComment(id, comment);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评论", description = "根据ID获取评论")
    public Result<Comment> getComment(@PathVariable Long id) {
        Comment comment = commentService.getComment(id);
        return Result.success(comment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除指定的评论")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询评论", description = "分页查询所有评论")
    public Result<IPage<Comment>> listComments(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Comment> page = commentService.listComments(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/reference/{referenceId}/{referenceType}")
    @Operation(summary = "根据引用查询评论", description = "根据引用查询相关的评论")
    public Result<IPage<Comment>> listCommentsByReference(
            @PathVariable Long referenceId,
            @PathVariable Integer referenceType,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Comment> page = commentService.listCommentsByReference(referenceId, referenceType, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "根据父评论ID查询回复", description = "根据父评论ID查询所有回复")
    public Result<IPage<Comment>> listRepliesByParentId(
            @PathVariable Long parentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Comment> page = commentService.listRepliesByParentId(parentId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询评论", description = "根据用户ID查询该用户的所有评论")
    public Result<IPage<Comment>> listCommentsByUserId(
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Comment> page = commentService.listCommentsByUserId(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索评论", description = "根据关键词搜索评论")
    public Result<IPage<Comment>> searchComments(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<Comment> page = commentService.searchComments(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/count/{referenceId}/{referenceType}")
    @Operation(summary = "统计评论数量", description = "统计引用相关的评论数量")
    public Result<Integer> countCommentsByReference(@PathVariable Long referenceId, @PathVariable Integer referenceType) {
        Integer count = commentService.countCommentsByReference(referenceId, referenceType);
        return Result.success(count);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论", description = "点赞指定的评论")
    public Result<Comment> likeComment(@PathVariable Long id) {
        Comment comment = commentService.likeComment(id);
        return Result.success(comment);
    }

    @PostMapping("/{id}/unlike")
    @Operation(summary = "取消点赞评论", description = "取消点赞指定的评论")
    public Result<Comment> unlikeComment(@PathVariable Long id) {
        Comment comment = commentService.unlikeComment(id);
        return Result.success(comment);
    }

    @PostMapping("/{id}/top")
    @Operation(summary = "置顶评论", description = "置顶指定的评论")
    public Result<Comment> topComment(@PathVariable Long id) {
        Comment comment = commentService.topComment(id);
        return Result.success(comment);
    }

    @PostMapping("/{id}/cancelTop")
    @Operation(summary = "取消置顶评论", description = "取消置顶指定的评论")
    public Result<Comment> cancelTopComment(@PathVariable Long id) {
        Comment comment = commentService.cancelTopComment(id);
        return Result.success(comment);
    }

    @PostMapping("/{id}/essence")
    @Operation(summary = "设置精华评论", description = "设置指定的评论为精华")
    public Result<Comment> essenceComment(@PathVariable Long id) {
        Comment comment = commentService.essenceComment(id);
        return Result.success(comment);
    }

    @PostMapping("/{id}/cancelEssence")
    @Operation(summary = "取消精华评论", description = "取消指定的评论精华状态")
    public Result<Comment> cancelEssenceComment(@PathVariable Long id) {
        Comment comment = commentService.cancelEssenceComment(id);
        return Result.success(comment);
    }
}