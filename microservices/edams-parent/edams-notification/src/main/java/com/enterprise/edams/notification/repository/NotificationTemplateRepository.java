package com.enterprise.edams.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知模板Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface NotificationTemplateRepository extends BaseMapper<NotificationTemplate> {

    /**
     * 根据模板编码查询
     */
    @Select("SELECT * FROM notification_template WHERE code = #{code} AND is_deleted = 0 LIMIT 1")
    NotificationTemplate findByCode(@Param("code") String code);

    /**
     * 检查模板编码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM notification_template WHERE code = #{code} AND is_deleted = 0")
    boolean existsByCode(@Param("code") String code);
}
