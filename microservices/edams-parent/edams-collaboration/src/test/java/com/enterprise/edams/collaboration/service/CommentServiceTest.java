package com.enterprise.edams.collaboration.service;

import com.enterprise.edams.collaboration.entity.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评论服务测试
 *
 * @author EDAMS Team
 */
@SpringBootTest
@ActiveProfiles("test")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    void testCreateComment() {
        Comment result = commentService.createComment(
                "data_asset", "asset-001", "这是一条测试评论", null, "user-001", "测试用户"
        );

        assertNotNull(result);
        assertEquals("data_asset", result.getBusinessType());
        assertEquals("asset-001", result.getBusinessId());
        assertEquals("这是一条测试评论", result.getContent());
        assertEquals("user-001", result.getUserId());
        assertEquals(0, result.getLikeCount());
        assertEquals(0, result.getReplyCount());
    }

    @Test
    void testCreateReply() {
        Comment parent = commentService.createComment(
                "data_asset", "asset-002", "父评论", null, "user-001", "测试用户"
        );

        Comment reply = commentService.createComment(
                "data_asset", "asset-002", "回复评论", parent.getId(), "user-002", "回复用户"
        );

        assertNotNull(reply);
        assertEquals(parent.getId(), reply.getParentId());

        // 验证父评论的回复数
        Comment updatedParent = commentService.getById(parent.getId());
        assertEquals(1, updatedParent.getReplyCount());
    }

    @Test
    void testGetCommentsByBusiness() {
        commentService.createComment("data_asset", "asset-003", "评论1", null, "user-001", "用户1");
        commentService.createComment("data_asset", "asset-003", "评论2", null, "user-002", "用户2");

        List<Comment> comments = commentService.getCommentsByBusiness("data_asset", "asset-003");

        assertNotNull(comments);
        assertEquals(2, comments.size());
    }

    @Test
    void testLikeComment() {
        Comment comment = commentService.createComment(
                "data_asset", "asset-004", "点赞测试", null, "user-001", "测试用户"
        );

        assertEquals(0, comment.getLikeCount());

        commentService.likeComment(comment.getId());

        Comment updated = commentService.getById(comment.getId());
        assertEquals(1, updated.getLikeCount());
    }
}
