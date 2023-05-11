package com.yilin.yixueblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专题表
 */
@Data
@TableName("tb_subject")
public class Subject extends SuperEntity<Subject> {

    private static final long serialVersionUID = 1L;

    /**
     * 专题名
     */
    private String subjectName;

    /**
     * 专题简介
     */
    private String summary;

    /**
     * 封面图片UID
     */
    private String fileUid;

    /**
     * 专题点击数
     */
    private String clickCount;

    /**
     * 专题收藏数
     */
    private String collectCount;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

    /**
     * 分类图
     */
    @TableField(exist = false)
    private Picture picture;
}
