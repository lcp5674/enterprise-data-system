package com.enterprise.edams.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
