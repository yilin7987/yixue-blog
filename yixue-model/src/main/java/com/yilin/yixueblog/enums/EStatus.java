package com.yilin.yixueblog.enums;

/**
 * 状态枚举类
 */
public enum EStatus {

    /**
     * 逻辑删除
     */
    DISABLED (0),
    /**
     * 激活
     */
    ENABLE (1),
    /**
     * 冻结
     */
    FREEZE (2),
    /**
     * 置顶
     */
    STICK(3);
    private final int key;

    EStatus(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
