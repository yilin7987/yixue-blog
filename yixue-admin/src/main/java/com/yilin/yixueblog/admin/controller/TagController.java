package com.yilin.yixueblog.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.Tag;
import com.yilin.yixueblog.service.service.TagService;
import com.yilin.yixueblog.model.vo.TagVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "增加标签", notes = "增加标签", response = String.class)
    @PostMapping("/add")
    public Result add(@RequestBody TagVO tagVO) {
        String addTag = tagService.addTag(tagVO);
//        log.info("增加标签");
        return Result.succeed().message(addTag);
    }

    @ApiOperation(value = "获取标签列表", notes = "获取标签列表", response = String.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody TagVO tagVO) {
//        log.info("获取标签列表");
        IPage<Tag> pageList = tagService.getPageList(tagVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "编辑标签", notes = "编辑标签", response = Result.class)
    @PostMapping("/edit")
    public Result edit(@RequestBody TagVO tagVO) {
//        log.info("编辑标签");
        String editTag = tagService.editTag(tagVO);
        return Result.succeed().message(editTag);
    }

    @ApiOperation(value = "删除标签", notes = "删除标签", response = Result.class)
    @PostMapping("/delete")
    public Result delete(@RequestBody List<String> tagUidList) {
//        log.info("删除标签");
        String deleteTag = tagService.deleteTag(tagUidList);
        return Result.succeed().message(deleteTag);
    }
}

