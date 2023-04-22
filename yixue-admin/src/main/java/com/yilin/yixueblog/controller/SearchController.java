package com.yilin.yixueblog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.service.BlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/search")
@Api(value = "SQL搜索相关接口", tags = {"SQL搜索相关接口"})
@Slf4j
public class SearchController {
    @Autowired
    private BlogService blogService;

    @ApiOperation(value = "搜索Blog", notes = "搜索Blog")
    @GetMapping("/searchBlog")
    public Result sqlSearchBlog(@ApiParam(name = "keywords", value = "关键字", required = true) @RequestParam(required = true) String keywords,
                                        @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                        @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(keywords) || StringUtils.isEmpty(keywords.trim())) {
            return Result.err().message("关键字不能为空");
        }
        Map<String, Object> blogMap = blogService.getBlogByKeyword(keywords, currentPage, pageSize);
        return Result.succeed().data(blogMap);

    }

    @ApiOperation(value = "根据标签获取相关的博客", notes = "根据标签获取相关的博客")
    @GetMapping("/searchBlogByTag")
    public Result searchBlogByTag(HttpServletRequest request,
                                  @ApiParam(name = "tagUid", value = "博客标签UID", required = true) @RequestParam(name = "tagUid", required = true) String tagUid,
                                  @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                  @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        if (StringUtils.isEmpty(tagUid)) {
            return Result.err().message("标签不能为空");
        }
        IPage<Blog> blogIPage = blogService.searchBlogByTag(tagUid, currentPage, pageSize);
        return Result.succeed().data(blogIPage);
    }

    @ApiOperation(value = "根据分类获取相关的博客", notes = "根据标签获取相关的博客")
    @GetMapping("/searchBlogBySort")
    public Result searchBlogBySort(HttpServletRequest request,
                                   @ApiParam(name = "blogSortUid", value = "博客分类UID", required = true) @RequestParam(name = "blogSortUid", required = true) String blogSortUid,
                                   @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                   @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        if (StringUtils.isEmpty(blogSortUid)) {
            return Result.err().message("uid不能为空");
        }
        IPage<Blog> blogIPage = blogService.searchBlogByBlogSort(blogSortUid, currentPage, pageSize);
        return Result.succeed().data(blogIPage);
    }
    @ApiOperation(value = "根据作者获取相关的博客", notes = "根据作者获取相关的博客")
    @GetMapping("/searchBlogByAuthor")
    public Result searchBlogByAuthor(HttpServletRequest request,
                                     @ApiParam(name = "author", value = "作者名称", required = true) @RequestParam(name = "author", required = true) String author,
                                     @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                     @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {
        if (StringUtils.isEmpty(author)) {
            return Result.err().message("作者不能为空");
        }
        IPage<Blog> blogIPage = blogService.searchBlogByAuthor(author, currentPage, pageSize);
        return Result.succeed().data(blogIPage);
    }
}
