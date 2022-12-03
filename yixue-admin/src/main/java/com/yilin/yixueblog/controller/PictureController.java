package com.yilin.yixueblog.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Picture;
import com.yilin.yixueblog.service.PictureService;
import com.yilin.yixueblog.vo.PictureVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 图片表 前端控制器
 * </p>
 * @author yilin
 * @since 2022-11-17
 */
@RestController
@RequestMapping("/admin/picture")
public class PictureController {
    @Autowired
    private PictureService pictureService;

    @ApiOperation(value = "上传图片", notes = "上传图片", response = Result.class)
    @PostMapping("/upload")
    public Result upload(@RequestBody MultipartFile file) throws IOException {
        Picture picture = pictureService.uploadPicture(file);
        if (StringUtils.isEmpty(picture.getTxUrl())){
            return Result.err().message("添加失败");
        }
//        log.info("添加图片:", pictureVOList);
        return Result.succeed().data(picture);
    }
    @ApiOperation(value = "增加图片", notes = "增加图片", response = String.class)
    @PostMapping("/add")
    public Result add(@RequestBody PictureVO pictureVO) {
//        log.info("添加图片:", pictureVOList);
        String addPicture = pictureService.addPicture(pictureVO);
        return Result.succeed().message(addPicture);
    }

    @ApiOperation(value = "获取图片列表", notes = "获取图片列表", response = Result.class)
    @PostMapping(value = "/getList")
    public Result getList(@RequestBody PictureVO pictureVO) {
//        log.info("获取图片列表:", pictureVO);
        IPage<Picture> pageList = pictureService.getPageList(pictureVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "删除图片", notes = "删除图片", response = Result.class)
    @PostMapping(value = "/delete")
    public Result deletePicture(@RequestBody List<String> pictureUidList) {
//        log.info("获取图片列表:", pictureVO);
        String s = pictureService.deletePicture(pictureUidList);
        return Result.succeed().message(s);
    }
}

