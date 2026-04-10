package com.enterprise.edams.collaboration.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.collaboration.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论数据访问层
 *
 * @author EDAMS Team
 */
@Mapper
public interface CommentRepository extends BaseMapper<Comment> {
}
