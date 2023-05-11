package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.Tag;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.TagMapper;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2022-11-19
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    @Lazy
    private TagService tagService;
    @Autowired
    @Lazy
    private BlogService blogService;

    /**
     * 新增博客标签
     *
     * @param tagVO
     */
    @Override
    public String addTag(TagVO tagVO) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("content", tagVO.getContent());
        queryWrapper.eq("status", EStatus.ENABLE);
        Tag tempTag = tagService.getOne(queryWrapper);
        if (tempTag != null) {
            return "标签已存在";
        }
        Tag tag = new Tag();
        tag.setContent(tagVO.getContent());
        tag.setClickCount(0);
        tag.setStatus(EStatus.ENABLE);
        tag.setSort(tagVO.getSort());
        tag.insert();
        // 删除Redis中的BLOG_TAG
//        deleteRedisBlogTagList();
        return "添加成功";
    }

    /**
     * 获取博客标签列表
     *
     * @param tagVO
     * @return
     */
    @Override
    public IPage<Tag> getPageList(TagVO tagVO) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(tagVO.getKeyword()) && !StringUtils.isEmpty(tagVO.getKeyword())) {
            queryWrapper.like("content", tagVO.getKeyword().trim());
        }

        Page<Tag> page = new Page<>();
        page.setCurrent(tagVO.getCurrentPage());
        page.setSize(tagVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        if (StringUtils.isNotEmpty(tagVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(tagVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        } else if (StringUtils.isNotEmpty(tagVO.getOrderByDescColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(tagVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            queryWrapper.orderByDesc("sort");
        }
        IPage<Tag> pageList = tagService.page(page, queryWrapper);
//        List<Tag> tagList = pageList.getRecords();
        return pageList;
    }

    /**
     * 编辑博客标签
     *
     * @param tagVO
     */
    @Override
    public String editTag(TagVO tagVO) {
        Tag tag = tagService.getById(tagVO.getUid());
        // 名字改变了 改变需要查找是否有相同名字的
        if (tag != null && !tag.getContent().equals(tagVO.getContent())) {
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("content", tagVO.getContent());
            queryWrapper.eq("status", EStatus.ENABLE);
            Tag tempTag = tagService.getOne(queryWrapper);
            if (tempTag != null) {
                return "标签已存在";
            }
        }

        tag.setContent(tagVO.getContent());
        tag.setStatus(EStatus.ENABLE);
        tag.setSort(tagVO.getSort());
        tag.updateById();
        return "修改成功";
    }

    /**
     * 删除标签
     *
     * @param tagUidList
     * @return
     */
    @Override
    public String deleteTag(List<String> tagUidList) {
        if (tagUidList.size() <= 0) {
            return "参数有误";
        }

        // 判断要删除的标签，是否有博客
        int blogCount = 0;
        for (String s : tagUidList) {
            QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
            blogQueryWrapper.eq("status", EStatus.ENABLE);
            blogQueryWrapper.like("tag_uid", s);
            blogCount = blogCount + (int) blogService.count(blogQueryWrapper);
        }
        if (blogCount > 0) {
            return "标签下还有博客";
        }

        List<Tag> tagList = tagService.listByIds(tagUidList);

        tagList.forEach(item -> {
            item.setStatus(EStatus.DISABLED);
        });
        Boolean save = tagService.updateBatchById(tagList);
        if (save) {
            return "删除成功";
        }
        return "删除失败";
    }
}
