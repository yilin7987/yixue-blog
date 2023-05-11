package com.yilin.yixueblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 图片分类表
 * </p>
 * @author yilin
 * @since 2022-11-20
 */
@Data
@TableName("tb_picture_sort")
public class PictureSort extends SuperEntity<PictureSort> {

    private static final long serialVersionUID=1L;

    /**
     * 父UID
     */
    private String parentUid;

    /**
     * 分类名
     */
    private String name;

    /**
     * 分类图片Uid
     */
    private String fileUid;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

    /**
     * 是否显示  1: 是  0: 否
     */
    private Integer isShow;

    //以下字段不存入数据库

    /**
     * 分类图
     */
    @TableField(exist = false)
    private List<Picture> photoList;

    /**
     * 分类封面
     */
    @TableField(exist = false)
    private Picture picture;


}
