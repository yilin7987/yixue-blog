package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.model.entity.Subject;
import com.yilin.yixueblog.service.service.SubjectService;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.SubjectItemMapper;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.SubjectItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.SubjectItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    @Lazy
    private SubjectItemService subjectItemService;
    @Autowired
    @Lazy
    private SubjectService subjectService;
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
        }else {
            QueryWrapper<SubjectItem> temp=new QueryWrapper<>();
            temp.orderByDesc("update_time");
            temp.last("LIMIT 1");
            SubjectItem subjectItem = subjectItemService.getOne(temp);
            queryWrapper.eq("subject_uid", subjectItem.getSubjectUid());
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

    /**
     * 通过创建时间排序专题列表
     *
     * @param subjectUid
     * @param isDesc
     * @return
     */
    @Override
    public String sortByCreateTime(String subjectUid, Boolean isDesc) {
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("subject_uid", subjectUid);
        // 查询出所有的专题列表
        List<SubjectItem> subjectItemList = baseMapper.selectList(queryWrapper);
        // 获取专题中的博客uid
        List<String> blogUidList = new ArrayList<>();
        subjectItemList.forEach(item -> {
            blogUidList.add(item.getBlogUid());
        });
        if(blogUidList.size() == 0) {
            return "该专题没有内容";
        }
        List<Blog> blogList = blogService.listByIds(blogUidList);
        List<Blog> tempBlogList = null;
        // 升序排列或降序排列
        if(isDesc) {
            tempBlogList = blogList.stream().sorted(Comparator.comparing(Blog::getCreateTime).reversed()).collect(Collectors.toList());
        } else {
            tempBlogList = blogList.stream().sorted(Comparator.comparing(Blog::getCreateTime)).collect(Collectors.toList());
        }

        // 设置初始化最大的sort值
        int maxSort = tempBlogList.size();
        Map<String, Integer> subjectItemSortMap = new HashMap<>();
        for (Blog item : tempBlogList) {
            subjectItemSortMap.put(item.getUid(), maxSort--);
        }

        // 设置更新后的排序值
        for (SubjectItem item : subjectItemList) {
            item.setSort(subjectItemSortMap.get(item.getBlogUid()));
        }
        subjectItemService.updateBatchById(subjectItemList);
        return "更新成功";
    }

    /**
     * 批量删除专题item
     * @param subjectItemVOList
     */
    @Override
    public String deleteBatchSubjectItem(List<SubjectItemVO> subjectItemVOList) {
        if (subjectItemVOList.size() == 0) {
            return "传入参数有误!";
        }
        List<String> uids = new ArrayList<>();
        subjectItemVOList.forEach(item -> {
            uids.add(item.getUid());
        });
        subjectItemService.removeByIds(uids);
        return "删除成功";
    }

    /**
     * 编辑专题item
     * @param subjectItemVOList
     */
    @Override
    public String editSubjectItemList(List<SubjectItemVO> subjectItemVOList) {
        List<String> subjectItemUidList = new ArrayList<>();
        subjectItemVOList.forEach(item -> {
            subjectItemUidList.add(item.getUid());
        });
        List<SubjectItem> temp = null;
        if (subjectItemUidList.size() > 0) {
            temp = subjectItemService.listByIds(subjectItemUidList);
            if (temp.size() > 0) {
                HashMap<String, SubjectItemVO> subjectItemVOHashMap = new HashMap<>();
                subjectItemVOList.forEach(item -> {
                    subjectItemVOHashMap.put(item.getUid(), item);
                });
                // 修改排序字段
                List<SubjectItem> subjectItemList = new ArrayList<>();
                temp.forEach(item -> {
                    SubjectItemVO subjectItemVO = subjectItemVOHashMap.get(item.getUid());
                    item.setSubjectUid(subjectItemVO.getSubjectUid());
                    item.setBlogUid(subjectItemVO.getBlogUid());
                    item.setStatus(EStatus.ENABLE);
                    item.setSort(subjectItemVO.getSort());
                    item.setUpdateTime(new Date());
                    subjectItemList.add(item);
                });
                subjectItemService.updateBatchById(subjectItemList);
            }
        }
        return "更新成功";
    }

    /**
     * 批量新增专题
     * @param subjectItemVOList
     */
    @Override
    public String addSubjectItemList(List<SubjectItemVO> subjectItemVOList) {
        List<String> blogUidList = new ArrayList<>();
        String subjectUid = "";
        for (SubjectItemVO subjectItemVO : subjectItemVOList) {
            blogUidList.add(subjectItemVO.getBlogUid());
            if (StringUtils.isEmpty(subjectUid) && StringUtils.isNotEmpty(subjectItemVO.getSubjectUid())) {
                subjectUid = subjectItemVO.getSubjectUid();
            }
        }
        // 查询SubjectItem中是否包含重复的博客
        QueryWrapper<SubjectItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("subject_uid", subjectUid);
        queryWrapper.in("blog_uid", blogUidList);
        List<SubjectItem> repeatSubjectItemList = subjectItemService.list(queryWrapper);
        // 找出重复的博客UID
        List<String> repeatBlogList = new ArrayList<>();
        repeatSubjectItemList.forEach(item -> {
            repeatBlogList.add(item.getBlogUid());
        });

        List<SubjectItem> subjectItemList = new ArrayList<>();
        for (SubjectItemVO subjectItemVO : subjectItemVOList) {
            if (StringUtils.isEmpty(subjectItemVO.getSubjectUid()) || StringUtils.isEmpty(subjectItemVO.getBlogUid())) {
                return "传入参数有误！";
            }
            // 判断是否重复添加
            if (repeatBlogList.contains(subjectItemVO.getBlogUid())) {
                continue;
            } else {
                SubjectItem subjectItem = new SubjectItem();
                subjectItem.setSubjectUid(subjectItemVO.getSubjectUid());
                subjectItem.setBlogUid(subjectItemVO.getBlogUid());
                subjectItem.setStatus(EStatus.ENABLE);
                subjectItemList.add(subjectItem);
            }
        }

        if (subjectItemList.size() == 0) {
            if (repeatBlogList.size() == 0) {
                return "插入失败";
            } else {
                return "插入失败" + "，已跳过" + repeatBlogList.size() + "个重复数据";
            }
        } else {
            subjectItemService.saveBatch(subjectItemList);
            if (repeatBlogList.size() == 0) {
                return "插入成功";
            } else {
                return "插入成功" + "，已跳过" + repeatBlogList.size() + "个重复数据，成功插入" + (subjectItemVOList.size() - repeatBlogList.size()) + "条数据";
            }
        }
    }
}
