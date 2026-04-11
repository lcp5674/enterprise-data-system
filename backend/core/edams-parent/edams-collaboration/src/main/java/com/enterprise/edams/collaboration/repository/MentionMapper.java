package com.enterprise.edams.collaboration.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.collaboration.entity.Mention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 提及Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface MentionMapper extends BaseMapper<Mention> {

    @Select("SELECT * FROM collaboration_mention WHERE mentioned_user_id = #{userId} AND status = #{status} AND deleted = 0")
    IPage<Mention> findByUserIdAndStatus(Page<Mention> page, @Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM collaboration_mention WHERE source_id = #{sourceId} AND source_type = #{sourceType} AND deleted = 0")
    IPage<Mention> findBySource(Page<Mention> page, @Param("sourceId") Long sourceId, @Param("sourceType") Integer sourceType);

    @Select("SELECT * FROM collaboration_mention WHERE mentioner_id = #{mentionerId} AND deleted = 0")
    IPage<Mention> findByMentionerId(Page<Mention> page, @Param("mentionerId") Long mentionerId);
}