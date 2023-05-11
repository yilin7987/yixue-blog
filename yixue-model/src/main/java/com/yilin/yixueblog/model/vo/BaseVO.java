package com.yilin.yixueblog.model.vo;

import lombok.Data;

/**
 * BaseVO   view object 表现层 基类对象
 */
@Data
public class BaseVO<T> extends PageInfo<T> {

    /**
     * 唯一UID
     */
    private String uid;
    /**
     * 状态
     */
    private Integer status;
}
