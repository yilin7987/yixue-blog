package com.yilin.yixueblog.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.utils.StringUtils;
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
    public Result sqlSearchBlog(HttpServletRequest request,
            @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
            @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        IPage<Blog> iPage = blogService.getBlogByKeyword(request, currentPage, pageSize);
        return Result.succeed().data(iPage);

    }

}
