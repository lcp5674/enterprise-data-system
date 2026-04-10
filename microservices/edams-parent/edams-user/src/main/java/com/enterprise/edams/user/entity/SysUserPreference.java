package com.enterprise.edams.user.entity;

import com.enterprise.edams.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 用户偏好设置实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_preference")
public class SysUserPreference extends BaseEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 偏好键
     */
    private String preferenceKey;

    /**
     * 偏好值
     */
    private String preferenceValue;

    /**
     * 偏好类型：USER-用户级 SYSTEM-系统级
     */
    private String preferenceType;
}
