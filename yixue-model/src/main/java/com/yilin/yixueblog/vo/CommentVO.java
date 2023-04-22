package com.yilin.yixueblog.vo;

import lombok.Data;
import lombok.ToString;

/**
 * CommentVO
 */
@ToString
@Data
public class CommentVO extends BaseVO<CommentVO> {

    /**
     * 用户uid
     */
    private String userUid;

    /**
     * 回复某条评论的uid
     */
    private String toUid;

    /**
     * 回复某个人的uid
     */
    private String toUserUid;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 评论类型： 0: 评论   1: 点赞
     */
    private Integer type;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 博客uid
     */
    private String blogUid;

    /**
     * 评论来源： MESSAGE_BOARD，ABOUT，BLOG_INFO 等
     */
    private String source;
}
