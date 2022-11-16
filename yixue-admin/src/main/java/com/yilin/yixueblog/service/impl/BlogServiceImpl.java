package com.yilin.yixueblog.service.impl;

import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.mapper.BlogMapper;
import com.yilin.yixueblog.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 博客表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2022-11-16
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

}
