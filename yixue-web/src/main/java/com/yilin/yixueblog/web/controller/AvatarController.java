package com.yilin.yixueblog.web.controller;

import com.yilin.yixueblog.model.entity.Avatar;
import com.yilin.yixueblog.service.service.AvatarService;
import com.yilin.yixueblog.service.service.PictureService;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.utils.StringUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
