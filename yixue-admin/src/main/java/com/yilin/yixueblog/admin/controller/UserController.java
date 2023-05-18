package com.yilin.yixueblog.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.User;
import com.yilin.yixueblog.model.vo.UserVO;
import com.yilin.yixueblog.service.service.UserService;
import com.yilin.yixueblog.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "用户相关接口", tags = {"用户相关接口"})
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @ApiOperation(value = "获取用户列表", notes = "获取用户列表", response = Result.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody UserVO userVO) {
        IPage<User> pageList = userService.getPageList(userVO);
        return Result.succeed().data(pageList);
    }

    @ApiOperation(value = "新增用户", notes = "新增用户", response = Result.class)
    @PostMapping("/add")
    public Result add(@RequestBody UserVO userVO) {
        String msg = userService.addUser(userVO);
        return Result.succeed().message(msg);
    }

    @ApiOperation(value = "编辑用户", notes = "编辑用户", response = Result.class)
    @PostMapping("/edit")
    public Result edit(@RequestBody UserVO userVO) {
        String msg = userService.editUserAdmin(userVO);
        return Result.succeed().message(msg);
    }

    @ApiOperation(value = "删除用户", notes = "删除用户", response = Result.class)
    @PostMapping("/delete")
    public Result delete(@RequestBody UserVO userVO) {
        String msg = userService.deleteUser(userVO);
        return Result.succeed().message(msg);
    }

    @ApiOperation(value = "重置用户密码", notes = "重置用户密码", response = Result.class)
    @PostMapping("/resetUserPassword")
    public Result resetUserPassword(@RequestBody UserVO userVO) {
        String msg = userService.resetUserPassword(userVO);
        return Result.succeed().message(msg);
    }
}
