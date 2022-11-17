package com.yilin.yixueblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 图片
 */
@TableName("tb_picture")
@Data
public class Picture extends SuperEntity<Picture>{
    private static final long serialVersionUID = 1L;

    private String picOldName;
    /**
     * 图片大小
     */
    private Long picSize;
    /**
     * 分类uid
     */
    private String picSortUid;

    /**
     * 图片扩展名
     */
    private String picExpandedName;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片url地址
     */
    private String picUrl;

    /**
     * 七牛云Url
     */
    private String txUrl;

}
