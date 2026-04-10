package com.enterprise.edams.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页信息
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Data
public class Pagination implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * 校验并设置分页参数
     */
    public void validate() {
        if (this.page == null || this.page < 1) {
            this.page = 1;
        }
        if (this.pageSize == null || this.pageSize < 1) {
            this.pageSize = 20;
        }
        if (this.pageSize > 100) {
            this.pageSize = 100;
        }
        if (!"asc".equalsIgnoreCase(this.sortOrder) && !"desc".equalsIgnoreCase(this.sortOrder)) {
            this.sortOrder = "desc";
        }
    }

    /**
     * 获取偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }
}
