package com.enterprise.edams.watermark.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 水印模板实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("watermark_template")
public class WatermarkTemplate extends BaseEntity {
    
    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;
    
    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;
    
    /**
     * 水印类型
     */
    @TableField("watermark_type")
    private WatermarkType watermarkType;
    
    /**
     * 水印内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 透明度
     */
    @TableField("opacity")
    private Double opacity;
    
    /**
     * 字体大小
     */
    @TableField("font_size")
    private Integer fontSize;
    
    /**
     * 字体颜色
     */
    @TableField("font_color")
    private String fontColor;
    
    /**
     * 旋转角度
     */
    @TableField("rotation")
    private Integer rotation;
    
    /**
     * X轴位置
     */
    @TableField("position_x")
    private String positionX;
    
    /**
     * Y轴位置
     */
    @TableField("position_y")
    private String positionY;
    
    /**
     * 重复平铺
     */
    @TableField("repeatable")
    private Boolean repeatable;
    
    /**
     * 状态
     */
    @TableField("status")
    private TemplateStatus status;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
}

enum WatermarkType {
    TEXT("文字水印"),
    IMAGE("图片水印"),
    SCREEN("屏幕水印"),
    DATABASE("数据库水印"),
    COMBINED("复合水印");
    
    private final String description;
    WatermarkType(String description) { this.description = description; }
}

enum TemplateStatus {
    ACTIVE("生效中"),
    INACTIVE("已停用");
    
    private final String description;
    TemplateStatus(String description) { this.description = description; }
}
