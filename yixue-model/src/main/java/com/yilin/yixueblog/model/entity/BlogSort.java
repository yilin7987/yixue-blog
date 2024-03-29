package com.yilin.yixueblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 *
 * 博客分类表
 *
 */
@Data
@TableName("tb_blog_sort")
public class BlogSort extends SuperEntity<BlogSort> {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名
     */
    private String sortName;

    /**
     * 分类介绍
     */
    private String content;

    /**
     * 点击数
     */
    private Integer clickCount;

    /**
     * 排序字段，数值越大，越靠前
     */
    private Integer sort;

}
