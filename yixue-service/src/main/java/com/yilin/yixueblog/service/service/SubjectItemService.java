package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.vo.SubjectItemVO;

import java.util.List;

/**
 * <p>
 * 专题Item表 服务类
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
public interface SubjectItemService extends IService<SubjectItem> {
    /**
     * 获取专题item列表
     * @param subjectItemVO
     * @return
     */
    IPage<SubjectItem> getPageList(SubjectItemVO subjectItemVO);
    /**
     * 通过创建时间排序专题列表
     * @param isDesc
     * @return
     */
    String sortByCreateTime(String subjectUid, Boolean isDesc);

    /**
     * 批量删除专题item
     * @param subjectItemVOList
     */
    String deleteBatchSubjectItem(List<SubjectItemVO> subjectItemVOList);

    /**
     * 编辑专题item
     * @param subjectItemVOList
     */
    String editSubjectItemList(List<SubjectItemVO> subjectItemVOList);

    /**
     * 批量新增专题
     * @param subjectItemVOList
     */
    String addSubjectItemList(List<SubjectItemVO> subjectItemVOList);
}
