package com.yilin.yixueblog.controller;

import com.yilin.yixueblog.*;
import com.yilin.yixueblog.entity.User;
import com.yilin.yixueblog.service.UserService;
import com.yilin.yixueblog.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RefreshScope
@RequestMapping("/user")
@Api(value = "登录管理相关接口", tags = {"登录管理相关接口"})
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserVO userVO) {
        String token = userService.login(userVO);
        if (token=="-1"){
            return Result.err().message("用户不存在");
        }else if (token=="0"){
            return Result.err().message("密码错误");
        }
        return Result.succeed().data(token);

    }

    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserVO userVO) {
        String result = userService.register(userVO);
        if (result.equals("注册成功")){
            return Result.succeed().message(result);
        }
        return Result.err().message(result);
    }

    @ApiOperation(value = "根据token获取用户信息", notes = "获取用户信息")
    @GetMapping("/verify/{accessToken}")
    public Result verifyUser(@PathVariable("accessToken") String accessToken) {
        String userInfo = redisUtil.get("USER_TOKEN:" + accessToken);
        if (StringUtils.isEmpty(userInfo)) {
            return Result.err().message("token令牌未被识别");
        } else {
            Map<String, Object> map = JsonUtils.jsonToMap(userInfo);
            return Result.succeed().data(map);
        }
    }

    @ApiOperation(value = "删除Token", notes = "删除Token")
    @RequestMapping("/deleteToken/{token}")
    public Result deleteToken(@PathVariable("token") String token) {
        redisUtil.delete("USER_TOKEN:" + token);
        return Result.succeed().message("删除成功");
    }

    @ApiOperation(value = "编辑用户信息", notes = "编辑用户信息")
    @PostMapping("/editUser")
    public Result editUser(HttpServletRequest request, @RequestBody UserVO userVO) {
        if (request.getAttribute("userUid") == null || request.getAttribute("token") == null) {
            return Result.err().message("token令牌未被识别");
        }
        if (userService.editUser(request,userVO)){
            return Result.succeed().message("修改成功~");
        }
        return  Result.err().message("编辑失败, 未找到该用户!");
    }

    @ApiOperation(value = "更新用户密码", notes = "更新用户密码")
    @PostMapping("/updateUserPwd")
    public Result updateUserPwd(HttpServletRequest request, @RequestParam(value = "oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd) {
        if (StringUtils.isEmpty(oldPwd)) {
            return Result.err().message("传入参数有误!");
        }
        if (request.getAttribute("userUid") == null || request.getAttribute("token") == null) {
            return Result.err().message("token令牌未被识别");
        }
        String userUid = request.getAttribute("userUid").toString();
        User user = userService.getById(userUid);
        // 判断旧密码是否一致
        if (user.getPassWord().equals(MD5Utils.string2MD5(oldPwd))) {
            user.setPassWord(MD5Utils.string2MD5(newPwd));
            user.updateById();
            return Result.succeed().message("修改密码成功");
        }
        return Result.err().message("输入密码有误！");
    }

    @ApiOperation(value = "获取邮箱验证码", notes = "获取邮箱验证码")
    @GetMapping("/getCode")
    public Result getCode(@RequestParam("email") String email){
        if (userService.getCode(email)){
            return Result.succeed().message("已发送验证码到邮箱");
        }
        return Result.err().message("系统错误");
    }
}
