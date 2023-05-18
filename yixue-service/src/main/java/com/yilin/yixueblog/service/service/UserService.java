package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.entity.User;
import com.yilin.yixueblog.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {


    /**
     * 获取用户数
     * @param status
     * @return
     */
     Integer getUserCount(int status);

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

    /**
     * 获取用户列表
     * @param userVO
     * @return
     */
     IPage<User> getPageList(UserVO userVO);
    /**
     * 新增用户
     * @param userVO
     */
     String addUser(UserVO userVO);

    /**
     * 编辑用户
     * @param userVO
     */
     String editUserAdmin(UserVO userVO);

    /**
     * 删除用户
     * @param userVO
     */
     String deleteUser(UserVO userVO);
    /**
     * 重置用户密码
     * @param userVO
     * @return
     */
     String resetUserPassword(UserVO userVO);
}
