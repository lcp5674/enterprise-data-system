package com.enterprise.edams.user.entity;

import com.enterprise.edams.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 部门实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 部门路径
     */
    private String path;

    /**
     * 树形路径
     */
    private String treePath;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 负责人ID
     */
    private String leaderId;

    /**
     * 负责人姓名
     */
    private String leaderName;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
