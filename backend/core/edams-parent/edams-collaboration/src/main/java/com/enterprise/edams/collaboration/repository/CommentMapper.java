package com.enterprise.edams.collaboration.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.collaboration.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评论Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM collaboration_comment WHERE reference_id = #{referenceId} AND reference_type = #{referenceType} AND deleted = 0")
    IPage<Comment> findByReference(Page<Comment> page, @Param("referenceId") Long referenceId, @Param("referenceType") Integer referenceType);

    @Select("SELECT * FROM collaboration_comment WHERE parent_id = #{parentId} AND deleted = 0")
    IPage<Comment> findByParentId(Page<Comment> page, @Param("parentId") Long parentId);

    @Select("SELECT * FROM collaboration_comment WHERE user_id = #{userId} AND deleted = 0")
    IPage<Comment> findByUserId(Page<Comment> page, @Param("userId") Long userId);

    @Select("SELECT * FROM collaboration_comment WHERE content LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0")
    IPage<Comment> searchByKeyword(Page<Comment> page, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM collaboration_comment WHERE reference_id = #{referenceId} AND reference_type = #{referenceType} AND deleted = 0")
    Integer countByReference(@Param("referenceId") Long referenceId, @Param("referenceType") Integer referenceType);
}