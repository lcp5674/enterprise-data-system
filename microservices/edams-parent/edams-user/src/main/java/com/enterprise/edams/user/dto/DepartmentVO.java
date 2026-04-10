package com.enterprise.edams.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门视图对象
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门视图对象")
public class DepartmentVO {

    @Schema(description = "部门ID")
    private String id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "部门编码")
    private String code;

    @Schema(description = "上级部门ID")
    private String parentId;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "部门路径")
    private String path;

    @Schema(description = "树形路径")
    private String treePath;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "负责人ID")
    private String leaderId;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "子部门")
    private List<DepartmentVO> children;

    @Schema(description = "用户数")
    private Integer userCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
