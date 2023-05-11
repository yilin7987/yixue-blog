package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.BlogSort;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.BlogSortMapper;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.BlogSortService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.BlogSortVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 博客分类表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2022-11-19
 */
@Service
public class BlogSortServiceImpl extends ServiceImpl<BlogSortMapper, BlogSort> implements BlogSortService {
    @Autowired
    @Lazy
    private BlogSortService blogSortService;

    @Autowired
    @Lazy
    private BlogService blogService;

    /**
     * 新增博客分类
     *
     * @param blogSortVO
     */
    @Override
    public String addBlogSort(BlogSortVO blogSortVO) {
        // 判断添加的分类是否存在
        QueryWrapper<BlogSort> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sort_name", blogSortVO.getSortName());
        queryWrapper.eq("status", EStatus.ENABLE);
        BlogSort tempSort = blogSortService.getOne(queryWrapper);
        if (tempSort != null) {
            return "分类已存在";
        }

        BlogSort blogSort = new BlogSort();
        blogSort.setContent(blogSortVO.getContent());
        blogSort.setSortName(blogSortVO.getSortName());
        blogSort.setSort(blogSortVO.getSort());
        blogSort.setStatus(EStatus.ENABLE);
        blogSort.insert();
        return "添加成功";
    }

    /**
     * 获取博客分类列表
     * @param blogSortVO
     * @return
     */
    @Override
    public IPage<BlogSort> getPageList(BlogSortVO blogSortVO) {
        QueryWrapper<BlogSort> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(blogSortVO.getKeyword()) && !StringUtils.isEmpty(blogSortVO.getKeyword().trim())) {
            queryWrapper.like("sort_name", blogSortVO.getKeyword().trim());
        }
        if (StringUtils.isNotEmpty(blogSortVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(blogSortVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        } else if (StringUtils.isNotEmpty(blogSortVO.getOrderByDescColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(blogSortVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            queryWrapper.orderByDesc("sort");
        }
        Page<BlogSort> page = new Page<>();
        page.setCurrent(blogSortVO.getCurrentPage());
        page.setSize(blogSortVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        IPage<BlogSort> pageList = blogSortService.page(page, queryWrapper);

        return pageList;
    }

    /**
     * 获取全部博客分类列表
     * @return
     */
    @Override
    public List<BlogSort> getAllList() {
        QueryWrapper<BlogSort> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("sort");
        List<BlogSort> blogSortList = blogSortService.list(queryWrapper);
        return blogSortList;
    }

    /**
     * 编辑博客分类
     *
     * @param blogSortVO
     */
    @Override
    public String editBlogSort(BlogSortVO blogSortVO) {
        BlogSort blogSort = blogSortService.getById(blogSortVO.getUid());
        /**
         * 判断需要编辑的博客分类是否存在
         */
        if (!blogSort.getSortName().equals(blogSortVO.getSortName())) {
            QueryWrapper<BlogSort> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sort_name", blogSortVO.getSortName());
            queryWrapper.eq("status", EStatus.ENABLE);
            BlogSort tempSort = blogSortService.getOne(queryWrapper);
            if (tempSort != null) {
                return "分类已存在!";
            }
        }
        blogSort.setContent(blogSortVO.getContent());
        blogSort.setSortName(blogSortVO.getSortName());
        blogSort.setSort(blogSortVO.getSort());
        blogSort.setStatus(EStatus.ENABLE);
        blogSort.updateById();
        return "修改成功";
    }

    /**
     * 批量删除博客分类
     *
     * @param blogSortUidList
     */
    @Override
    public String deleteBlogSort(List<String> blogSortUidList) {
        if (blogSortUidList.size() <= 0) {
            return "参数有误";
        }

        // 判断要删除的分类，是否有博客
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("status", EStatus.ENABLE);
        blogQueryWrapper.in("blog_sort_uid", blogSortUidList);
        Integer blogCount = Math.toIntExact(blogService.count(blogQueryWrapper));
        if (blogCount > 0) {
            return "该分类下还有博客";
        }

        List<BlogSort> blogSortList = blogSortService.listByIds(blogSortUidList);
        blogSortList.forEach(item -> {
            item.setStatus(EStatus.DISABLED);
        });
        Boolean save = blogSortService.updateBatchById(blogSortList);
        if (save) {
            return "删除成功";
        }
        return "删除失败";
    }
}
