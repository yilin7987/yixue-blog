package com.yilin.yixueblog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * <p>
 * 头像表
 * </p>
 *
 * @author yilin
 * @since 2023-03-25
 */
@Data
@TableName("tb_avatar")
public class Avatar extends SuperEntity<Avatar> {

    private static final long serialVersionUID = 1L;


    @TableField("avatar_old_name")
    private String avatarOldName;

    @TableField("avatar_name")
    private String avatarName;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("avatar_expanded_name")
    private String avatarExpandedName;

    @TableField("avatar_size")
    private Long avatarSize;

    @TableField("admin_uid")
    private String adminUid;

    @TableField("user_uid")
    private String userUid;


}
