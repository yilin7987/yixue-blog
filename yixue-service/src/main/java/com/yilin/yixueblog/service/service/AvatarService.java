package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.entity.Avatar;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 * 头像表 服务类
 * </p>
 *
 * @author yilin
 * @since 2023-03-25
 */
public interface AvatarService extends IService<Avatar> {
    /**
     * 头像上传
     * @param multipartFile
     * @return
     */
    Avatar upload(HttpServletRequest request, MultipartFile multipartFile) throws IOException;

}
