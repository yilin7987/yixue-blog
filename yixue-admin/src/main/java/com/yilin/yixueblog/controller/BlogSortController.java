package com.yilin.yixueblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.entity.BlogSort;
import com.yilin.yixueblog.service.BlogService;
import com.yilin.yixueblog.service.BlogSortService;
import com.yilin.yixueblog.vo.BlogSortVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/admin/blogSort")
public class BlogSortController {
    @Autowired
    private BlogSortService blogSortService;
    @Autowired
    private BlogService blogService;

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
    public Result getBlogByBlogSortUid(HttpServletRequest request,
                                          @ApiParam(name = "blogSortUid", value = "分类UID", required = false) @RequestParam(name = "blogSortUid", required = false) String blogSortUid,
                                          @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                          @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(blogSortUid)) {
            return Result.err().message("传入BlogSortUid不能为空");
        }
        IPage<Blog> blogList = blogService.getBlogByBlogSortUid(blogSortUid, currentPage, pageSize);
        return Result.succeed().data(blogList);
    }

}

