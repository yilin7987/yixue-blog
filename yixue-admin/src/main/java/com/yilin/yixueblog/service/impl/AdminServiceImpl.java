package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.mapper.AdminMapper;
import com.yilin.yixueblog.entity.Admin;
import com.yilin.yixueblog.service.AdminService;
import org.springframework.stereotype.Service;
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    /**
     * 登录
     * @param username     账号
     * @param password     密码
     * @return  Integer  -1:用户不存在  0：密码不正确  1：验证通过
     */
    @Override
    public Integer login(String username, String password) {
        QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_name",username);
        Admin admin = baseMapper.selectOne(queryWrapper);
        if (admin==null){
            return -1;
        }else if(!admin.getPassword().equals(password)){
            return 0;
        }
        return 1;
    }
}
