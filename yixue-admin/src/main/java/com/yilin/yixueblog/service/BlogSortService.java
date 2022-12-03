package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.entity.BlogSort;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.vo.BlogSortVO;

import java.util.List;

/**
 * <p>
 * 博客分类表 服务类
 * </p>
 *
 * @author yilin
 * @since 2022-11-19
 */
public interface BlogSortService extends IService<BlogSort> {

    /**
     * 新增博客分类
     * @param blogSortVO
     */
    public String addBlogSort(BlogSortVO blogSortVO);

    /**
     * 获取博客分类列表
     * @param blogSortVO
     * @return
     */
    IPage<BlogSort> getPageList(BlogSortVO blogSortVO);

    /**
     * 编辑博客分类
     * @param blogSortVO
     */
    String editBlogSort(BlogSortVO blogSortVO);

    /**
     * 批量删除博客分类
     * @param blogSortUidList
     */
    String deleteBlogSort(List<String> blogSortUidList);
}
