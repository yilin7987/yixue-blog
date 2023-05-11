package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.entity.User;
import com.yilin.yixueblog.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    /**
     * 登录功能
     *
     * @param userVO
     * @return
     */
    String login(UserVO userVO);

    /**
     * 注册
     *
     * @param userVO
     * @return
     */
    String register(UserVO userVO);

    /**
     * 编辑个人信息
     *
     * @param userVO
     * @return
     */
    boolean editUser(HttpServletRequest request, UserVO userVO);

    /**
     * 发送邮件
     * @return
     */
    boolean getCode(String email);

}
