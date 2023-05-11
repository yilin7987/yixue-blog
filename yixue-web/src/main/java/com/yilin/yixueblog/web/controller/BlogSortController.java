package com.yilin.yixueblog.web.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.BlogSort;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.BlogSortService;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.utils.StringUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 博客分类表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2022-11-19
 */
@RestController
@RequestMapping("/blogSort")
public class BlogSortController {
    @Autowired
    private BlogSortService blogSortService;
    @Autowired
    private BlogService blogService;

    /**
     * 获取全部分类的信息
     */
    @ApiOperation(value = "获取全部分类的信息", notes = "获取全部分类的信息")
    @GetMapping("/getBlogSortList")
    public Result getBlogSortList() {
        List<BlogSort> allList = blogSortService.getAllList();
        return Result.succeed().data(allList);
    }

    @ApiOperation(value = "通过blogSortUid获取文章", notes = "通过blogSortUid获取文章")
    @GetMapping("/getBlogByBlogSortUid")
    public Result getBlogByBlogSortUid(@ApiParam(name = "blogSortUid", value = "分类UID") @RequestParam(name = "blogSortUid", required = false) String blogSortUid,
                                       @ApiParam(name = "currentPage", value = "当前页数") @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                       @ApiParam(name = "pageSize", value = "每页显示数目") @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(blogSortUid)) {
            return Result.err().message("传入BlogSortUid不能为空");
        }
        IPage<Blog> blogList = blogService.getBlogByBlogSortUid(blogSortUid, currentPage, pageSize);
        return Result.succeed().data(blogList);
    }

}

