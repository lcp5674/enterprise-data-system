package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据权限配置实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@TableName("sys_data_permission")
public class SysDataPermission {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 角色ID
     */
    private String roleId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 数据权限类型: DEPT=部门, DEPT_AND_CHILD=部门及子部门, CUSTOM_DEPT=自定义部门, DATA_RANGE=数据范围
     */
    private String permissionType;

    /**
     * 允许访问的部门ID列表(JSON格式)
     */
    private String allowedDeptIds;

    /**
     * 数据权限规则(JSON格式)
     */
    private String permissionRules;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
