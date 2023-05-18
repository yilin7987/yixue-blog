package com.yilin.yixueblog.web.controller;

import com.yilin.yixueblog.service.service.WebVisitService;
import com.yilin.yixueblog.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
public class IndexController {
    @Autowired
    private WebVisitService webVisitService;

    @ApiOperation(value = "记录访问页面", notes = "记录访问页面")
    @GetMapping("/recorderVisitPage")
    public Result recorderVisitPage(HttpServletRequest request) {
        webVisitService.addWebVisit(request);
        return Result.succeed();
    }
}
