package com.yilin.yixueblog.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.yilin.yixueblog.service.service.SubjectItemService;
import com.yilin.yixueblog.model.vo.SubjectItemVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 专题Item表 前端控制器
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
@RestController
@RequestMapping("/subjectItem")
public class SubjectItemController {
    @Autowired
    private SubjectItemService subjectItemService;

    @ApiOperation(value = "获取专题Item列表", notes = "获取专题Item列表", response = String.class)
    @PostMapping("/getItemList")
    public Result getItemList(@RequestBody SubjectItemVO subjectItemVO) {
        IPage<SubjectItem> list = subjectItemService.getPageList(subjectItemVO);
        return Result.succeed().data(list);
    }

}
