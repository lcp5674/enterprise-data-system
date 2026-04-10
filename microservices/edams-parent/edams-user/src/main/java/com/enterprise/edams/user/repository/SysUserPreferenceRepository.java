package com.enterprise.edams.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.user.entity.SysUserPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户偏好设置Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysUserPreferenceRepository extends BaseMapper<SysUserPreference> {

    /**
     * 根据用户ID查询所有偏好设置
     */
    @Select("SELECT * FROM sys_user_preference WHERE user_id = #{userId}")
    List<SysUserPreference> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和偏好键查询
     */
    @Select("SELECT * FROM sys_user_preference WHERE user_id = #{userId} AND preference_key = #{key} LIMIT 1")
    SysUserPreference findByUserIdAndKey(@Param("userId") String userId, @Param("key") String key);
}
