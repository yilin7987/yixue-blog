package com.yilin.yixueblog.model.vo;


import lombok.Data;

/**
 * SubjectItemVO

 */
@Data
public class SubjectItemVO extends BaseVO<SubjectItemVO> {

    /**
     * 专题UID
     */
    private String subjectUid;

    /**
     * 博客UID
     */
    private String blogUid;

    /**
     * 排序字段，数值越大，越靠前
     */
    private int sort;

}
