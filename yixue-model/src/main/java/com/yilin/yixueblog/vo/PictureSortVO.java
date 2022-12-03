package com.yilin.yixueblog.vo;

import lombok.Data;
import lombok.ToString;

/**
 * 相册分类实体类
 */
@ToString
@Data
public class PictureSortVO extends BaseVO<PictureSortVO> {

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

    /**
     * 有图片的情况下是否删除 1: 是  0: 否
     */
    private Integer isDelete;
}
