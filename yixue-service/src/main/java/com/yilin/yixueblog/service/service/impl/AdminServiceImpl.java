package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.entity.Avatar;
import com.yilin.yixueblog.service.mapper.AdminMapper;
import com.yilin.yixueblog.model.entity.Admin;
import com.yilin.yixueblog.service.service.AdminService;
import com.yilin.yixueblog.service.service.AvatarService;
import com.yilin.yixueblog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 登录
     * @param username     账号
     * @param password     密码
     * @return  Integer  -1:用户不存在  0：密码不正确  1：验证通过
     */
    @Override
    public String login(String username, String password) {
        QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_name",username);
        Admin admin = baseMapper.selectOne(queryWrapper);
        if (admin==null){
            return "-1";
        }else if(!admin.getPassword().equals(MD5Utils.string2MD5(password))){
            return "0";
        }
        // 更新登录信息
        HttpServletRequest request = RequestHolder.getRequest();
        admin.setLastLoginIp(IpUtils.getIpAddr(request));
        admin.setLastLoginTime(new Date());
        // 登录成功后，次数+1
        admin.setLoginCount(admin.getLoginCount() + 1);
        admin.updateById();
        // 获取用户头像
        if (StringUtils.isNotEmpty(admin.getAvatar())){
            Avatar avatar = avatarService.getById(admin.getAvatar());
            admin.setPhotoUrl(avatar.getAvatarUrl());
        }
        // 过滤密码
        admin.setPassword("");
        // 生成token
        String token = StringUtils.getUUID();
        //将从用户的数据缓存到redis中
        redisUtil.setEx("ADMIN_TOKEN:" + token, JsonUtils.objectToJson(admin), 7, TimeUnit.DAYS);
        return token;
    }
}
