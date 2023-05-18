package com.yilin.yixueblog.admin.controller;

import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.CommentService;
import com.yilin.yixueblog.service.service.UserService;
import com.yilin.yixueblog.service.service.WebVisitService;
import com.yilin.yixueblog.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
public class IndexController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private WebVisitService webVisitService;
    @Autowired
    private UserService userService;

    @ApiOperation(value = "首页初始化数据", notes = "首页初始化数据", response = String.class)
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public Result init() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("blogCount", blogService.getBlogCount(EStatus.ENABLE));
        map.put("commentCount", commentService.getCommentCount(EStatus.ENABLE));
        map.put("userCount", userService.getUserCount(EStatus.ENABLE));
        map.put("visitCount", webVisitService.getWebVisitCount());
        return Result.succeed().data(map);
    }

    @ApiOperation(value = "获取每个标签下文章数目", notes = "获取每个标签下文章数目", response = Result.class)
    @RequestMapping(value = "/getBlogCountByTag", method = RequestMethod.GET)
    public Result getBlogCountByTag() {
        List<Map<String, Object>> blogCountByTag = blogService.getBlogCountByTag();
        return Result.succeed().data(blogCountByTag);
    }

    @ApiOperation(value = "获取每个分类下文章数目", notes = "获取每个分类下文章数目", response = Result.class)
    @RequestMapping(value = "/getBlogCountByBlogSort", method = RequestMethod.GET)
    public Result getBlogCountByBlogSort() {

        List<Map<String, Object>> blogCountByBlogSort = blogService.getBlogCountByBlogSort();
        return Result.succeed().data(blogCountByBlogSort);
    }
}
