package com.yilin.yixueblog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Comment;
import com.yilin.yixueblog.model.entity.CommentReport;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.model.vo.CommentVO;
import com.yilin.yixueblog.model.vo.UserVO;
import com.yilin.yixueblog.service.service.CommentReportService;
import com.yilin.yixueblog.service.service.CommentService;
import com.yilin.yixueblog.utils.RedisUtil;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
@RequestMapping("/comment")
@Api(value = "评论相关接口", tags = {"评论相关接口"})
@Slf4j
public class CommentController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentReportService commentReportService;

    @ApiOperation(value = "获取用户收到的评论回复数", notes = "获取用户收到的评论回复数")
    @GetMapping("/getUserReceiveCommentCount")
    public Result getUserReceiveCommentCount(HttpServletRequest request) {
        // 判断用户是否登录
        Integer commentCount = 0;
        if (request.getAttribute("userUid") != null) {
            String userUid = request.getAttribute("userUid").toString();
            String redisKey = "USER_RECEIVE_COMMENT_COUNT:" + userUid;
            String count = redisUtil.get(redisKey);
            if (StringUtils.isNotEmpty(count)) {
                commentCount = Integer.valueOf(count);
            }
        }
        return Result.succeed().data(commentCount);
    }

    @ApiOperation(value = "增加评论", notes = "增加评论")
    @PostMapping("/add")
    public Result add( @RequestBody CommentVO commentVO) {
        String result = commentService.add(commentVO);
        if ("成功".equals(result)){
            return Result.succeed();
        }
        return Result.err().message(result);
    }

    /**
     * 获取评论列表
     * @param commentVO
     * @return
     */
    @ApiOperation(value = "获取评论列表", notes = "获取评论列表")
    @PostMapping("/getList")
    public Result getList(@RequestBody CommentVO commentVO) {
        IPage<Comment> page = commentService.getList(commentVO);
        return Result.succeed().data(page);
    }

    @ApiOperation(value = "获取用户的评论列表和回复", notes = "获取评论列表和回复")
    @PostMapping("/getListByUser")
    public Result getListByUser(HttpServletRequest request, @RequestBody UserVO userVO){
        Map<String, Object> map = commentService.getListByUser(request, userVO);
        if (map==null){
            return Result.err().message("token令牌未被识别");
        }
        return Result.succeed().data(map);
    }

    @ApiOperation(value = "阅读用户接收的评论数", notes = "阅读用户接收的评论数")
    @PostMapping("/readUserReceiveCommentCount")
    public Result readUserReceiveCommentCount(HttpServletRequest request) {
        // 判断用户是否登录
        if (request.getAttribute("userUid") != null) {
            String userUid = request.getAttribute("userUid").toString();
            String redisKey = "USER_RECEIVE_COMMENT_COUNT:" + userUid;
            redisUtil.delete(redisKey);
        }
        return Result.succeed().message("阅读成功");
    }

    @ApiOperation(value = "获取用户点赞信息", notes = "增加评论")
    @PostMapping("/getPraiseListByUser")
    public Result getPraiseListByUser(@ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                      @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        IPage<Comment> listByUser = commentService.getPraiseListByUser(currentPage, pageSize);
        if (listByUser!=null){
            return Result.succeed().data(listByUser);
        }
        return Result.err().message("token令牌未被识别");
    }

    @ApiOperation(value = "通过Uid给博客点赞", notes = "通过Uid给博客点赞")
    @GetMapping("/praiseBlogByUid")
    public Result praiseBlogByUid(@ApiParam(name = "blogUid", value = "博客UID", required = false) @RequestParam(name = "blogUid", required = false) String blogUid) {
        if (StringUtils.isEmpty(blogUid)) {
            return Result.err().message("传入参数有误");
        }
        return commentService.praiseBlogByUid(blogUid);
    }

    @ApiOperation(value = "判定用户是否点赞", notes = "判定用户是否点赞")
    @GetMapping("/isPraise")
    public Result isPraise(@RequestParam(name = "blogUid", required = false) String blogUid) {
        if (StringUtils.isEmpty(blogUid)) {
            return Result.err().message("传入参数有误");
        }
        String msg = commentService.isPraise(blogUid);
        if ( msg != null ){
            return Result.succeed().message(msg);
        }
        return Result.err().message("点赞系统出现错误");
    }

    @ApiOperation(value = "举报评论", notes = "举报评论")
    @PostMapping("/report")
    public Result reportComment(@RequestBody CommentVO commentVO) {
        Comment comment = commentService.getById(commentVO.getUid());
        // 判断评论是否被删除
        if (comment == null || comment.getStatus() == EStatus.DISABLED) {
            return Result.err().message("该评论不存在");
        }

        // 判断举报的评论是否是自己的
        if (comment.getUserUid().equals(commentVO.getUserUid())) {
            return Result.err().message("不能举报自己的评论");
        }
        // 查看该用户是否重复举报该评论
        QueryWrapper<CommentReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_uid", commentVO.getUserUid());
        queryWrapper.eq("report_comment_uid", comment.getUid());
        List<CommentReport> commentReportList = commentReportService.list(queryWrapper);
        if (commentReportList.size() > 0) {
            return Result.err().message("不能重复举报该评论");
        }
        CommentReport commentReport = new CommentReport();
        commentReport.setContent(commentVO.getContent());
        commentReport.setProgress(0);
        // 从VO中获取举报的用户uid
        commentReport.setUserUid(commentVO.getUserUid());
        commentReport.setReportCommentUid(comment.getUid());
        // 从entity中获取被举报的用户uid
        commentReport.setReportUserUid(comment.getUserUid());
        commentReport.setStatus(EStatus.ENABLE);
        commentReport.insert();

        return Result.succeed().message("举报成功");
    }

}
