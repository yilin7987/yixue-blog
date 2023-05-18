package com.yilin.yixueblog.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.yilin.yixueblog.service.service.SubjectItemService;
import com.yilin.yixueblog.model.vo.SubjectItemVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/getList")
    public Result getItemList(@RequestBody SubjectItemVO subjectItemVO) {
        IPage<SubjectItem> list = subjectItemService.getPageList(subjectItemVO);
        return Result.succeed().data(list);
    }

    @ApiOperation(value = "通过创建时间排序专题列表", notes = "通过创建时间排序专题列表", response = Result.class)
    @PostMapping("/sortByCreateTime")
    public Result sortByCreateTime(@ApiParam(name = "subjectUid", value = "专题uid") @RequestParam(name = "subjectUid", required = true) String subjectUid,
                                   @ApiParam(name = "isDesc", value = "是否从大到小排列") @RequestParam(name = "isDesc", required = false, defaultValue = "false") Boolean isDesc) {
        String msg = subjectItemService.sortByCreateTime(subjectUid, isDesc);
        if("更新成功".equals(msg)){
            return Result.succeed().message(msg);
        }
        return Result.err().message(msg);
    }

    @ApiOperation(value = "批量删除专题Item", notes = "批量删除专题Item", response = Result.class)
    @PostMapping("/deleteBatch")
    public Result delete(@RequestBody List<SubjectItemVO> subjectItemVOList) {
        String msg = subjectItemService.deleteBatchSubjectItem(subjectItemVOList);
        if ("删除成功".equals(msg)){
            return Result.succeed().message(msg);
        }
        return Result.err().message(msg);
    }


    @ApiOperation(value = "编辑专题Item", notes = "编辑专题Item", response = Result.class)
    @PostMapping("/edit")
    public Result edit(@RequestBody List<SubjectItemVO> subjectItemVOList) {
        String msg = subjectItemService.editSubjectItemList(subjectItemVOList);
        return Result.succeed().message(msg);
    }

    @ApiOperation(value = "增加专题Item", notes = "增加专题Item", response = Result.class)
    @PostMapping("/add")
    public Result add(@RequestBody List<SubjectItemVO> subjectItemVOList, BindingResult result) {
        String msg = subjectItemService.addSubjectItemList(subjectItemVOList);
        if ("插入成功".equals(msg.substring(0,4))){
            return Result.succeed().message(msg);
        }
        return Result.err().message(msg);
    }
}
