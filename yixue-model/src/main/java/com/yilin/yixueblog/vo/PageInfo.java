package com.yilin.yixueblog.vo;


import lombok.Data;

/**
 * PageVO  用于分页
 */
@Data
public class PageInfo<T> {

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 当前页
     */
    private Long currentPage;

    /**
     * 页大小
     */
    private Long pageSize;
}
