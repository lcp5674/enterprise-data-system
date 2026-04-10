package com.enterprise.edams.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 分页数据
     */
    private PageData<T> data;

    /**
     * 分页数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageData<T> {
        /**
         * 数据列表
         */
        private List<T> records;

        /**
         * 总记录数
         */
        private long total;

        /**
         * 当前页码
         */
        private long pageNum;

        /**
         * 每页大小
         */
        private long pageSize;

        /**
         * 总页数
         */
        private long pages;

        /**
         * 是否有下一页
         */
        private boolean hasNext;

        /**
         * 是否有上一页
         */
        private boolean hasPrevious;
    }

    /**
     * 成功响应
     */
    public static <T> PageResult<T> success(List<T> records, long total, long pageNum, long pageSize) {
        long pages = (total + pageSize - 1) / pageSize;
        return PageResult.<T>builder()
                .code(200)
                .message("操作成功")
                .data(PageData.<T>builder()
                        .records(records)
                        .total(total)
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .pages(pages)
                        .hasNext(pageNum < pages)
                        .hasPrevious(pageNum > 1)
                        .build())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> PageResult<T> fail(String message) {
        return PageResult.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}
