package com.yilin.yixueblog.service.impl;

import com.yilin.yixueblog.COSUtils;
import com.yilin.yixueblog.FileUtils;
import com.yilin.yixueblog.entity.Avatar;
import com.yilin.yixueblog.entity.Picture;
import com.yilin.yixueblog.entity.User;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.AvatarMapper;
import com.yilin.yixueblog.service.AvatarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * 头像表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2023-03-25
 */
@Service
public class AvatarServiceImpl extends ServiceImpl<AvatarMapper, Avatar> implements AvatarService {
    @Autowired
    COSUtils cosUtils;
    @Autowired
    UserService userService;
    /**
     * 头像上传
     *
     * @param file
     * @return
     */
    @Override
    public Avatar upload(HttpServletRequest request, MultipartFile file) throws IOException {
        //获取用户id
        String userUid = request.getAttribute("userUid").toString();

        Avatar avatar = new Avatar();
        //文件名字
        String originalFilename = file.getOriginalFilename();
        avatar.setAvatarOldName(originalFilename);
        //获取扩展名，默认是jpg
        String contentType = FileUtils.getPicExpandedName(originalFilename);

        //后缀名
        avatar.setAvatarExpandedName(contentType);
        //文件大小
        long size = file.getSize();
        avatar.setAvatarSize(size);
        avatar.setStatus(EStatus.ENABLE);

        avatar.setUserUid(userUid);

        avatar.insert();

        //更新用户的头像信息
        User user = userService.getById(userUid);
        user.setAvatar(avatar.getUid());

        String uploadStr;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();

            uploadStr = cosUtils.upload(inputStream, avatar.getUid(), "avatar/"+avatar.getAvatarExpandedName());
            if (!"上传失败".equals(uploadStr)) {
                avatar.setAvatarUrl(uploadStr);
                avatar.setAvatarName(avatar.getUid() + "." + avatar.getAvatarExpandedName());
                avatar.updateById();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
        return avatar;
    }
}
