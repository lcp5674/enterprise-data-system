package com.enterprise.dataplatform.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("device_group")
@Document(collection = "device_groups")
public class DeviceGroup {

    @Id
    private String id;

    @TableField("group_id")
    private String groupId;

    @TableField("group_name")
    private String groupName;

    @TableField("description")
    private String description;

    @TableField("parent_id")
    private String parentId;

    @TableField("hierarchy_path")
    private String hierarchyPath;

    @TableField("device_count")
    private Integer deviceCount;

    @TableField("tags")
    private java.util.Map<String, String> tags;

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
