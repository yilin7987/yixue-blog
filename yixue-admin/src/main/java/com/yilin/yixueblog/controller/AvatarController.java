package com.yilin.yixueblog.controller;

import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Avatar;
import com.yilin.yixueblog.service.AvatarService;
import com.yilin.yixueblog.service.PictureService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 头像表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2023-03-25
 */
@RestController
@RequestMapping("/avatar")
public class AvatarController {
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private PictureService pictureService;

    @ApiOperation(value = "头像上传", notes = "头像上传")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public Result upload(HttpServletRequest request, @RequestBody MultipartFile file) throws IOException {
        Avatar avatar = avatarService.upload(request, file);
        if (StringUtils.isEmpty(avatar.getAvatarUrl())){
            return Result.err().message("添加失败");
        }
        return Result.succeed().data(avatar);
    }

}
