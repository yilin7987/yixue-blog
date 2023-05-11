package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.entity.Admin;

public interface AdminService extends IService<Admin> {
    /**
     * 登录
     * @param username     账号
     * @param password     密码
     * @return  Integer  -1:用户不存在  0：密码不正确  1：验证通过
     */
    String login(String username, String password);

}
