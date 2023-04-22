package com.yilin.yixueblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.service.BlogService;
import com.yilin.yixueblog.vo.BlogVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 博客表 前端控制器
 * @author yilin
 * @since 2022-11-16
 */
@RestController
@RequestMapping("/admin/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @ApiOperation(value = "获取博客列表", notes = "获取博客列表", response = String.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody BlogVO blogVO) {
        IPage<Blog> pageList = blogService.getPageList(blogVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "增加博客", notes = "增加博客", response = String.class)
    @PostMapping("/add")
    public Result add(@RequestBody BlogVO blogVO) {

        String addBlog = blogService.addBlog(blogVO);
        return Result.succeed().message(addBlog);
    }

    @ApiOperation(value = "编辑博客", notes = "编辑博客", response = Result.class)
    @PostMapping("/edit")
    public Result edit(@RequestBody BlogVO blogVO) {
        String editBlog = blogService.editBlog(blogVO);
        return Result.succeed().message(editBlog);
    }

    @ApiOperation(value = "删除博客", notes = "删除博客", response = Result.class)
    @PostMapping("/delete")
    public Result delete(@RequestBody List<String> blogUidList) {
        String deleteBlog = blogService.deleteBlog(blogUidList);
        return Result.succeed().message(deleteBlog);
    }

    @ApiOperation(value = "推荐博客修改", notes = "推荐博客修改", response = Result.class)
    @PostMapping("/editRecommend")
    public Result editRecommend(@RequestBody List<BlogVO> blogVOList) {
        String str = blogService.editRecommendBlog(blogVOList);
        return Result.succeed().message(str);
    }

    @ApiOperation(value = "通过推荐等级获取博客列表", notes = "通过推荐等级获取博客列表", response = Result.class)
    @GetMapping("/getBlogByLevel")
    public Result getBlogByLevel(HttpServletRequest request,
                                 @ApiParam(name = "level", value = "推荐等级", required = false) @RequestParam(name = "level", required = false, defaultValue = "0") Integer level,
                                 @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                 @ApiParam(name = "useSort", value = "使用排序", required = false) @RequestParam(name = "useSort", required = false, defaultValue = "0") Integer useSort) {
        List<Blog> blogList = blogService.getBlogPageByLevel(level, currentPage, useSort);
        return Result.succeed().data(blogList);
    }

    @ApiOperation(value = "通过Uid获取博客内容", notes = "通过Uid获取博客内容")
    @GetMapping("/getBlogByUid")
    public Result getBlogByUid(@ApiParam(name = "blogUid", value = "博客UID", required = false) @RequestParam(name = "blogUid", required = false) String blogUid) {
        Blog blog = blogService.getBlogByUid(blogUid);
        return Result.succeed().data(blog);
    }

    @ApiOperation(value = "获取首页点击排行博客", notes = "获取首页点击排行博客")
    @GetMapping("/getHotBlog")
    public Result getHotBlog() {
//        log.info("获取首页点击排行博客");
        List<Blog> hotBlog = blogService.getHotBlog();
        return Result.succeed().data(hotBlog);
    }

    /**
     * 获取归档的信息(日期信息)
     */
    @ApiOperation(value = "归档", notes = "归档")
    @GetMapping("/getArchiveList")
    public Result getSortList() {
        List list = blogService.getBlogTimeList();
        return Result.succeed().data(list);
    }

    @ApiOperation(value = "通过月份获取文章", notes = "通过月份获取文章")
    @GetMapping("/getBlogByMonth")
    public Result getArticleByMonth(@ApiParam(name = "monthDate", value = "归档的日期", required = false) @RequestParam(name = "monthDate", required = false) String monthDate) {
        List<Blog> blogByMonth = blogService.getBlogByMonth(monthDate);
        return Result.succeed().data(blogByMonth);
    }
    @ApiOperation(value = "通过uid获取相似文章", notes = "通过uid获取相似文章")
    @GetMapping("/getSimilarityBlogUid")
    public Result getSimilarityBlogUid(@ApiParam(name = "blogUid", value = "博客uid") @RequestParam(name = "blogUid", required = false) String blogUid){
        List<Blog> blogs = blogService.getSimilarityBlogUid(blogUid);
        return Result.succeed().data(blogs);
    }
}

