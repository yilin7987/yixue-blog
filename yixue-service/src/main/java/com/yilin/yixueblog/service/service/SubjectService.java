package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Subject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.vo.SubjectVO;

/**
 * <p>
 * 专题表 服务类
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
public interface SubjectService extends IService<Subject> {

    /**
     * 获取专题列表
     * @param subjectVO
     * @return
     */
    IPage<Subject> getPageList(SubjectVO subjectVO);

}
