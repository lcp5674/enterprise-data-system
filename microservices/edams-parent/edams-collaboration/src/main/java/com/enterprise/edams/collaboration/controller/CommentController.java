package com.enterprise.edams.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.collaboration.entity.Comment;
import com.enterprise.edams.collaboration.service.CommentService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "发表评论")
    public Result<Comment> createComment(
            @RequestParam String businessType,
            @RequestParam String businessId,
            @RequestParam String content,
            @RequestParam(required = false) String parentId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(commentService.createComment(businessType, businessId, content, parentId, userId, userName));
    }

    @GetMapping("/business/{businessType}/{businessId}")
    @Operation(summary = "查询业务的评论")
    public Result<List<Comment>> getCommentsByBusiness(
            @PathVariable String businessType,
            @PathVariable String businessId) {
        return Result.success(commentService.getCommentsByBusiness(businessType, businessId));
    }

    @GetMapping
    @Operation(summary = "分页查询评论")
    public Result<Page<Comment>> listComments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessId) {
        Page<Comment> page = new Page<>(current, size);
        return Result.success(commentService.listComments(page, businessType, businessId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable String id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论")
    public Result<Void> likeComment(@PathVariable String id) {
        commentService.likeComment(id);
        return Result.success();
    }
}
