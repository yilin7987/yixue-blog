package com.yilin.yixueblog.service.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.entity.WebVisit;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public interface WebVisitService extends IService<WebVisit> {

    /**
     * 增加访问记录
     * @param request
     */
     void addWebVisit(HttpServletRequest request);

    /**
     * 获取今日网站访问人数
     *
     * @return
     */
     int getWebVisitCount();

}
