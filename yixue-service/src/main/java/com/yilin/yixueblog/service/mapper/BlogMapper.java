package com.yilin.yixueblog.service.mapper;

import com.yilin.yixueblog.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 博客表 Mapper 接口
 * </p>
 *
 * @author yilin
 * @since 2022-11-16
 */
public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 获取每个分类的博客数量
     * @return
     */
    @Select("SELECT blog_sort_uid, COUNT(blog_sort_uid) AS count FROM  tb_blog where status = 1 GROUP BY blog_sort_uid")
    List<Map<String, Object>> getBlogCountByBlogSort();
    @Select("SELECT tag_uid, COUNT(tag_uid) as count FROM  tb_blog where status = 1 GROUP BY tag_uid")
    List<Map<String, Object>> getBlogCountByTag();
}
