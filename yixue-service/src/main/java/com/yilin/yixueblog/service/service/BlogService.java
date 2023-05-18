package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.vo.BlogVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 博客表 服务类
 */
public interface BlogService extends IService<Blog> {
    /**
     * 通过关键字搜索博客列表
     * @param request
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Blog> getBlogByKeyword(HttpServletRequest request, Long currentPage, Long pageSize);
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

    /**
     * 通过推荐等级获取博客Page
     * @param level       推荐级别
     * @param currentPage 当前页
     * @param useSort     是否使用排序字段
     * @return
     */
    List<Blog> getBlogPageByLevel(Integer level, Long currentPage, Integer useSort);

    /**
     * 通过Uid查找Blog
     * @param uid
     * @return
     */
    Blog getBlogByUid(String uid);

    /**
     * 获取博客点击排行
     * @return
     */
    List<Blog> getHotBlog();
    /**
     * 通过标签搜索博客
     * @param tagUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<Blog> getBlogByTagUid(String tagUid, Long currentPage, Long pageSize);
    /**
     * 获取博客的归档日期
     * @return
     */
    List getBlogTimeList();
    /**
     * 通过月份获取日期
     * @param monthDate
     * @return
     */
    List<Blog> getBlogByMonth(String monthDate);

    /**
     * 通过博客分类UID获取博客列表
     * @param blogSortUid
     * @param currentPage
     * @param pageSize
     * @return
     */
     IPage<Blog> getBlogByBlogSortUid(String blogSortUid, Long currentPage, Long pageSize);

    /**
     * 加工blog
     * @param blogList
     * @return
     */
    List<Blog> setBlog(List<Blog> blogList);

    /**
     * 通过博客uid获取相似博客
     * @param blogUid
     * @return
     */
    List<Blog> getSimilarityBlogUid(String blogUid);

    /**
     * 通过状态获取博客数量
     */
     Integer getBlogCount(Integer status);

    /**
     * 通过分类获取博客数目
     */
     List<Map<String, Object>> getBlogCountByBlogSort();

    /**
     * 通过标签获取博客数目
     */
     List<Map<String, Object>> getBlogCountByTag();

}
