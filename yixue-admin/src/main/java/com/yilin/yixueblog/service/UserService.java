package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.entity.User;
import com.yilin.yixueblog.vo.UserVO;

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
