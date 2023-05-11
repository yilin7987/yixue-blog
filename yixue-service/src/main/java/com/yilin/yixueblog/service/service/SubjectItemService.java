package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.vo.SubjectItemVO;

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

}
