package com.yilin.yixueblog.vo;

import lombok.Data;

@Data
public class PictureVO extends BaseVO<PictureVO>{
    /**
     * 图片uid
     */
    private String picUid;
    /**
     * 所属相册分类UID
     */
    private String picSortUid;
}
