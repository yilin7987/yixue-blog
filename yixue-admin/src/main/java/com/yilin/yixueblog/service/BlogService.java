package com.yilin.yixueblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.vo.BlogVO;

/**
 * 博客表 服务类
 */
public interface BlogService extends IService<Blog> {
    /**
     * 获取博客列表
     * @param blogVO
     * @return
     */
    public IPage<Blog> getPageList(BlogVO blogVO);
}
