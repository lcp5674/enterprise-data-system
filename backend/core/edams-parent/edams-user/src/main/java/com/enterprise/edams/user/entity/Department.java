package com.enterprise.edams.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 部门实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_department")
public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 部门名称 */
    private String name;

    /** 部门编码（唯一） */
    private String code;

    /** 父部门ID（顶级为0或null） */
    private Long parentId;

    /** 部门层级（从0开始） */
    private Integer level;

    /** 排序号 */
    private Integer sortOrder;

    /** 负责人ID */
    private Long leaderId;

    /** 负责人姓名 */
    private String leaderName;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 部门描述 */
    private String description;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 租户ID */
    private Long tenantId;

    /** 全路径编码（如：/001/001002/） */
    private String treePath;
}
