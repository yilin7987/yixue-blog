package com.yilin.yixueblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * 评论举报表
 * </p>
 */
@Data
@TableName("tb_comment_report")
public class CommentReport extends SuperEntity<CommentReport> {

    private static final long serialVersionUID = 1L;

    /**
     * 举报人UID
     */
    private String userUid;

    /**
     * 被举报的评论Uid
     */
    private String reportCommentUid;

    /**
     * 被举报的用户uid
     */
    private String reportUserUid;


    /**
     * 举报原因
     */
    private String content;

    /**
     * 进展状态: 0 未查看   1: 已查看  2：已处理
     */
    private Integer progress;

}
