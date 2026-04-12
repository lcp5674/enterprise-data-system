package com.enterprise.edams.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_catalog")
public class Catalog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String type;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
