package com.yilin.yixueblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.entity.BlogSort;
import com.yilin.yixueblog.service.BlogSortService;
import com.yilin.yixueblog.vo.BlogSortVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/admin/blogSort")
public class BlogSortController {
    @Autowired
    private BlogSortService blogSortService;

    @ApiOperation(value = "增加博客分类", notes = "增加博客分类", response = String.class)
    @PostMapping("/add")
    public Result add(@RequestBody BlogSortVO blogSortVO) {
//        log.info("增加博客分类");
        String blogSort = blogSortService.addBlogSort(blogSortVO);
        return Result.succeed().message(blogSort);
    }
    @ApiOperation(value = "获取博客分类列表", notes = "获取博客分类列表", response = String.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody BlogSortVO blogSortVO) {
//        log.info("获取博客分类列表");
        IPage<BlogSort> pageList = blogSortService.getPageList(blogSortVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "编辑博客分类", notes = "编辑博客分类", response = Result.class)
    @PostMapping("/edit")
    public Result edit(@RequestBody BlogSortVO blogSortVO) {
//        log.info("编辑博客分类");
        String editBlogSort = blogSortService.editBlogSort(blogSortVO);
        return Result.succeed().message(editBlogSort);
    }

    @ApiOperation(value = "删除博客分类", notes = "删除博客分类", response = Result.class)
    @PostMapping("/delete")
    public Result delete(@RequestBody List<String> blogSortUidList) {
//        log.info("删除博客分类");
        String deleteBlogSort = blogSortService.deleteBlogSort(blogSortUidList);
        return Result.succeed().message(deleteBlogSort);
    }

}

