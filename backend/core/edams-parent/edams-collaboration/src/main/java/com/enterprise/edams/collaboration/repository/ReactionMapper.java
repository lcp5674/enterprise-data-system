package com.enterprise.edams.collaboration.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.collaboration.entity.Reaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 反应Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ReactionMapper extends BaseMapper<Reaction> {

    @Select("SELECT * FROM collaboration_reaction WHERE comment_id = #{commentId} AND deleted = 0")
    IPage<Reaction> findByCommentId(Page<Reaction> page, @Param("commentId") Long commentId);

    @Select("SELECT * FROM collaboration_reaction WHERE user_id = #{userId} AND deleted = 0")
    IPage<Reaction> findByUserId(Page<Reaction> page, @Param("userId") Long userId);

    @Select("SELECT * FROM collaboration_reaction WHERE comment_id = #{commentId} AND reaction_type = #{reactionType} AND deleted = 0")
    IPage<Reaction> findByCommentIdAndType(Page<Reaction> page, @Param("commentId") Long commentId, @Param("reactionType") Integer reactionType);

    @Select("SELECT COUNT(*) FROM collaboration_reaction WHERE comment_id = #{commentId} AND reaction_type = #{reactionType} AND deleted = 0")
    Integer countByCommentIdAndType(@Param("commentId") Long commentId, @Param("reactionType") Integer reactionType);
}