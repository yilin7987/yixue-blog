package com.yilin.yixueblog.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.entity.WebVisit;
import com.yilin.yixueblog.service.mapper.WebVisitMapper;
import com.yilin.yixueblog.service.service.WebVisitService;
import com.yilin.yixueblog.utils.DateUtils;
import com.yilin.yixueblog.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class WebVisitServiceImpl extends ServiceImpl<WebVisitMapper, WebVisit> implements WebVisitService {

    @Resource
    private WebVisitMapper webVisitMapper;



    @Override
    public void addWebVisit(HttpServletRequest request) {

        //增加记录（可以考虑使用AOP）
        Map<String, String> map = IpUtils.getOsAndBrowserInfo(request);
        String os = map.get("OS");
        String browser = map.get("BROWSER");
        WebVisit webVisit = new WebVisit();
        String ip = IpUtils.getIpAddr(request);

        QueryWrapper<WebVisit> queryWrapper=new QueryWrapper<>();
        queryWrapper.ge("create_time", DateUtils.getToDayStartTime());
        queryWrapper.le("create_time",DateUtils.getToDayEndTime());
        queryWrapper.in("ip",ip);
        List<WebVisit> webVisitList = baseMapper.selectList(queryWrapper);
        if (webVisitList.size()!=0){
            return;
        }
        webVisit.setIp(ip);
        webVisit.setOs(os);
        webVisit.setBrowser(browser);
        webVisit.insert();
    }

    @Override
    public int getWebVisitCount() {
        // 获取今日开始和结束时间
        String startTime = DateUtils.getToDayStartTime();
        String endTime = DateUtils.getToDayEndTime();
        return webVisitMapper.getIpCount(startTime, endTime);
    }


}
