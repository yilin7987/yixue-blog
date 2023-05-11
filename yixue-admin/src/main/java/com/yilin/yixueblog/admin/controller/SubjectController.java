package com.yilin.yixueblog.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.Subject;
import com.yilin.yixueblog.service.service.SubjectService;
import com.yilin.yixueblog.model.vo.SubjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 专题表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
@Api(value = "专题相关接口", tags = {"专题相关接口"})
@RestController
@RequestMapping("/subject")
@Slf4j
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @ApiOperation(value = "获取专题列表", notes = "获取专题列表", response = Result.class)
    @PostMapping("/getList")
    public Result getList(@RequestBody SubjectVO subjectVO) {
        IPage<Subject> subjectIPage = subjectService.getPageList(subjectVO);
        return Result.succeed().data(subjectIPage);
    }
}
