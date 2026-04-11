package com.enterprise.edams.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知模板Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {

    @Select("SELECT * FROM sys_notification_template WHERE code = #{code} AND deleted = 0 AND status = 1")
    NotificationTemplate findByCode(@Param("code") String code);
}
