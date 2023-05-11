package com.yilin.yixueblog.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.PictureSort;
import com.yilin.yixueblog.service.service.PictureSortService;
import com.yilin.yixueblog.model.vo.PictureSortVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 图片分类表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2022-11-20
 */
@RestController
@RequestMapping("/pictureSort")
public class PictureSortController {
    @Autowired
    private PictureSortService pictureSortService;

    @ApiOperation(value = "增加图片分类", notes = "增加图片分类", response = Result.class)
    @PostMapping("/add")
    public Result add(@RequestBody PictureSortVO pictureSortVO) {
//        log.info("增加图片分类: {}", pictureSortVO);
        String s = pictureSortService.addPictureSort(pictureSortVO);
        return Result.succeed().message(s);
    }

    @ApiOperation(value = "获取图片分类列表", notes = "获取图片分类列表", response = Result.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody PictureSortVO pictureSortVO) {
//        log.info("增加图片分类: {}", pictureSortVO);
        IPage<PictureSort> pageList = pictureSortService.getPageList(pictureSortVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "编辑图片分类", notes = "编辑图片分类", response = Result.class)
    @PostMapping("/edit")
    public Result edit( @RequestBody PictureSortVO pictureSortVO) {
//        log.info("编辑图片分类: {}", pictureSortVO);
        String editPictureSort = pictureSortService.editPictureSort(pictureSortVO);
        return Result.succeed().message(editPictureSort);
    }

    @ApiOperation(value = "删除图片分类", notes = "删除图片分类", response = Result.class)
    @PostMapping("/delete")
    public Result delete(@RequestBody PictureSortVO pictureSortVO) {
//        log.info("删除图片分类: {}", pictureSortUidList);
        String deletePictureSort = pictureSortService.deletePictureSort(pictureSortVO);
        return Result.succeed().message(deletePictureSort);
    }
}

