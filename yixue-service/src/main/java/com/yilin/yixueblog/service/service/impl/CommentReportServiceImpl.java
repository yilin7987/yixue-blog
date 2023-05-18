package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.entity.CommentReport;
import com.yilin.yixueblog.service.mapper.CommentReportMapper;
import com.yilin.yixueblog.service.service.CommentReportService;
import org.springframework.stereotype.Service;

/**
 * 评论举报表 服务实现类
 */
@Service
public class CommentReportServiceImpl extends ServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {

}
