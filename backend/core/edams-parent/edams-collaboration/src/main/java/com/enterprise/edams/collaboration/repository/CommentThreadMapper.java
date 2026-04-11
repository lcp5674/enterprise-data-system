package com.enterprise.edams.collaboration.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.collaboration.entity.CommentThread;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评论线程Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface CommentThreadMapper extends BaseMapper<CommentThread> {

    @Select("SELECT * FROM collaboration_comment_thread WHERE reference_id = #{referenceId} AND reference_type = #{referenceType} AND deleted = 0")
    IPage<CommentThread> findByReference(Page<CommentThread> page, @Param("referenceId") Long referenceId, @Param("referenceType") Integer referenceType);

    @Select("SELECT * FROM collaboration_comment_thread WHERE creator_id = #{creatorId} AND deleted = 0")
    IPage<CommentThread> findByCreatorId(Page<CommentThread> page, @Param("creatorId") Long creatorId);

    @Select("SELECT * FROM collaboration_comment_thread WHERE status = #{status} AND deleted = 0")
    IPage<CommentThread> findByStatus(Page<CommentThread> page, @Param("status") Integer status);
}