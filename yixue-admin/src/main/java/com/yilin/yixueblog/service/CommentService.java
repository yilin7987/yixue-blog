package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.entity.Comment;
import com.yilin.yixueblog.vo.CommentVO;
import com.yilin.yixueblog.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface CommentService extends IService<Comment> {
    /**
     * 发表评论
     *
     * @param commentVO
     * @return
     */
    String add(CommentVO commentVO);

    /**
     * 获取评论列表
     *
     * @param commentVO
     * @return
     */
    IPage<Comment> getList(CommentVO commentVO);

    /**
     * 获取用户的评论
     *
     * @param userVO
     * @return
     */
    public Map<String, Object> getListByUser(HttpServletRequest request, UserVO userVO);

    /**
     * 获取用户点赞
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Comment> getPraiseListByUser(Long currentPage,Long pageSize);

    Result praiseBlogByUid(String blogUid);

    /**
     * 判定用户是否点赞
     * @param blogUid
     * @return
     */
    boolean isPraise(String blogUid);
}
