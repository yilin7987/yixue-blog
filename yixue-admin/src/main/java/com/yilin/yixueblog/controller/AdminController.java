package com.yilin.yixueblog.controller;

import com.yilin.yixueblog.IpUtils;
import com.yilin.yixueblog.RedisUtil;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.service.AdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 登录功能
     * @param request   用来获取ip
     * @param userName  用户名
     * @param password  密码
     * @param isRememberMe  记住密码
     * @return     Result
     */
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public Result login(HttpServletRequest request,
                        @ApiParam(name = "username", value = "用户名或邮箱或手机号") @RequestParam(name = "username", required = false) String userName,
                        @ApiParam(name = "password", value = "密码") @RequestParam(name = "password", required = false) String password,
                        @ApiParam(name = "isRememberMe", value = "是否记住账号密码") @RequestParam(name = "isRememberMe", required = false, defaultValue = "false") Boolean isRememberMe){
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            return Result.err().message("账号或密码不能为空");
        }
        //获取ip
        String ip = IpUtils.getIpAddr(request);
        String limitCount = redisUtil.get("login_limit:" + ip);
        if (StringUtils.isNotEmpty(limitCount)) {
            Integer tempLimitCount = Integer.valueOf(limitCount);
            if (tempLimitCount >= 5) {
                return Result.err().message("密码输错次数过多,已被锁定30分钟");
            }
        }
        Integer i = adminService.login(userName,password);
        if (i==-1){
            return Result.err().message("用户不存在");
        }else if(i==0){
            Integer loginCommit = setLoginCommit(request);
            return Result.err().message("密码错误，还有"+loginCommit+"次机会");
        }
        return Result.succeed().message("登录成功");
    }

    /**
     * 设置登录限制，返回剩余次数
     * 密码错误五次，将会锁定30分钟
     * @param request
     */
    private Integer setLoginCommit(HttpServletRequest request) {
        //获取ip
        String ip = IpUtils.getIpAddr(request);
        String count = redisUtil.get("login_limit:" + ip);
        //限制5次
        int surplusCount = 5;
        if (StringUtils.isNotEmpty(count)) {
            //次数+1
            Integer countTemp = Integer.parseInt(count) + 1;
            surplusCount = surplusCount - countTemp;
            redisUtil.setEx("login_limit:" + ip, String.valueOf(countTemp), 30, TimeUnit.MINUTES);
        } else {
            surplusCount = surplusCount - 1;
            redisUtil.setEx("login_limit:" + ip, String.valueOf(1), 30, TimeUnit.MINUTES);
        }
        return surplusCount;
    }

}
