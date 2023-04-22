package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.entity.SubjectItem;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.SubjectItemMapper;
import com.yilin.yixueblog.service.BlogService;
import com.yilin.yixueblog.service.SubjectItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.vo.SubjectItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 专题Item表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
@Service
public class SubjectItemServiceImpl extends ServiceImpl<SubjectItemMapper, SubjectItem> implements SubjectItemService {

    @Autowired
    private BlogService blogService;
    /**
     * 获取专题item列表
     * @param subjectItemVO
     * @return
     */
    @Override
    public IPage<SubjectItem> getPageList(SubjectItemVO subjectItemVO) {
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper<>();
        Page<SubjectItem> page = new Page<>();
        if (StringUtils.isNotEmpty(subjectItemVO.getSubjectUid())) {
            queryWrapper.eq("subject_uid", subjectItemVO.getSubjectUid());
        }
        page.setCurrent(subjectItemVO.getCurrentPage());
        page.setSize(subjectItemVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("sort");
        IPage<SubjectItem> pageList = baseMapper.selectPage(page, queryWrapper);
        List<SubjectItem> subjectItemList = pageList.getRecords();

        //博客uid
        List<String> blogUidList = new ArrayList<>();
        subjectItemList.forEach(item -> {
            blogUidList.add(item.getBlogUid());
        });
        if (blogUidList.size() > 0) {
            List<Blog> blogList = blogService.listByIds(blogUidList);
            if (blogList.size() > 0) {
                blogList = blogService.setBlog(blogList);
                Map<String, Blog> blogMap = new HashMap<>();
                blogList.forEach(item -> {
                    blogMap.put(item.getUid(), item);
                });
                subjectItemList.forEach(item -> {
                    item.setBlog(blogMap.get(item.getBlogUid()));
                });
                pageList.setRecords(subjectItemList);
            }
        }

        return pageList;
    }
}
