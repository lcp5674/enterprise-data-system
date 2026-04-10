package com.enterprise.edams.watermark.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 水印记录实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("watermark_record")
public class WatermarkRecord extends BaseEntity {
    
    /**
     * 记录编号
     */
    @TableField("record_no")
    private String recordNo;
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 资产名称
     */
    @TableField("asset_name")
    private String assetName;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    
    /**
     * 原始文件路径
     */
    @TableField("original_path")
    private String originalPath;
    
    /**
     * 水印文件路径
     */
    @TableField("watermarked_path")
    private String watermarkedPath;
    
    /**
     * 水印类型
     */
    @TableField("watermark_type")
    private WatermarkType watermarkType;
    
    /**
     * 模板ID
     */
    @TableField("template_id")
    private Long templateId;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;
    
    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;
    
    /**
     * 水印内容
     */
    @TableField("watermark_content")
    private String watermarkContent;
    
    /**
     * 状态
     */
    @TableField("status")
    private WatermarkStatus status;
    
    /**
     * 处理时长(毫秒)
     */
    @TableField("process_time")
    private Long processTime;
}

enum WatermarkStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    SUCCESS("成功"),
    FAILED("失败");
    
    private final String description;
    WatermarkStatus(String description) { this.description = description; }
}
