package com.yilin.yixueblog.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.model.vo.BlogVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 博客表 前端控制器
 * @author yilin
 * @since 2022-11-16
 */
@RestController
@RequestMapping("/blog")
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


}

