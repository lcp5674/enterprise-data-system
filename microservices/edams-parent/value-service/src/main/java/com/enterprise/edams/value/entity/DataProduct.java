package com.enterprise.edams.value.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据产品实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("data_product")
public class DataProduct extends BaseEntity {
    
    /**
     * 产品编码
     */
    @TableField("product_code")
    private String productCode;
    
    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;
    
    /**
     * 产品描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 产品类型
     */
    @TableField("product_type")
    private ProductType productType;
    
    /**
     * 版本号
     */
    @TableField("version")
    private String version;
    
    /**
     * 产品状态
     */
    @TableField("status")
    private ProductStatus status;
    
    /**
     * 关联资产ID(JSON数组)
     */
    @TableField("asset_ids")
    private String assetIds;
    
    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;
    
    /**
     * 单位
     */
    @TableField("price_unit")
    private String priceUnit;
    
    /**
     * 销量
     */
    @TableField("sales_count")
    private Integer salesCount;
    
    /**
     * 评分
     */
    @TableField("rating")
    private BigDecimal rating;
    
    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;
    
    /**
     * 标签(JSON)
     */
    @TableField("tags")
    private String tags;
    
    /**
     * 预览数据
     */
    @TableField("preview_data")
    private String previewData;
    
    /**
     * 产品文档
     */
    @TableField("documentation")
    private String documentation;
}

/**
 * 产品类型枚举
 */
enum ProductType {
    RAW_DATA("原始数据"),
    PROCESSED_DATA("加工数据"),
    ANALYTICAL_RESULT("分析结果"),
    DATA_REPORT("数据报告"),
    DATA_API("数据API"),
    DATA_MODEL("数据模型"),
    OTHER("其他");
    
    private final String description;
    
    ProductType(String description) {
        this.description = description;
    }
}

/**
 * 产品状态枚举
 */
enum ProductStatus {
    DRAFT("草稿"),
    PENDING_REVIEW("待审核"),
    PUBLISHED("已发布"),
    OFFLINE("已下线"),
    ARCHIVED("已归档");
    
    private final String description;
    
    ProductStatus(String description) {
        this.description = description;
    }
}
