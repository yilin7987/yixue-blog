package com.yilin.yixueblog.web.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.Tag;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.model.vo.TagVO;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.TagService;
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
 * 标签表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2022-11-19
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;
    @Autowired
    private BlogService blogService;

    @ApiOperation(value = "获取标签列表", notes = "获取标签列表", response = String.class)
    @GetMapping("/getList")
    public Result getList() {
//        log.info("获取标签列表");
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("sort");
        List<Tag> tagList = tagService.list(queryWrapper);
        return Result.succeed().data(tagList);
    }

    @ApiOperation(value = "通过TagUid获取文章", notes = "通过TagUid获取文章")
    @GetMapping("/getBlogByTagUid")
    public Result getArticleByTagUid(HttpServletRequest request,
                                             @ApiParam(name = "tagUid", value = "标签UID") @RequestParam(name = "tagUid", required = false) String tagUid,
                                             @ApiParam(name = "currentPage", value = "当前页数") @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                             @ApiParam(name = "pageSize", value = "每页显示数目") @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        if (StringUtils.isEmpty(tagUid)) {
            return Result.err().message("传入TagUid不能为空");
        }
        IPage<Blog> blogIPage = blogService.getBlogByTagUid(tagUid, currentPage, pageSize);
        return Result.succeed().data(blogIPage);
    }

}

