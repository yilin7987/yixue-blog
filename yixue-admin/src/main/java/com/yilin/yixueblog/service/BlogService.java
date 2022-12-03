package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.vo.BlogVO;

import java.util.List;

/**
 * 博客表 服务类
 */
public interface BlogService extends IService<Blog> {
    /**
     * 获取博客列表
     *
     * @param blogVO
     * @return
     */
    IPage<Blog> getPageList(BlogVO blogVO);

    /**
     * 新增博客
     *
     * @param blogVO
     */
    String addBlog(BlogVO blogVO);

    /**
     * 编辑博客
     *
     * @param blogVO
     */
    String editBlog(BlogVO blogVO);

    /**
     * 删除blog（逻辑删除 修改状态为0）
     * @param blogUidList
     * @return
     */
    String deleteBlog(List<String> blogUidList);

    /**
     * 修改推荐博客
     * @param blogVOList
     * @return
     */
    String editRecommendBlog(List<BlogVO> blogVOList);
}
