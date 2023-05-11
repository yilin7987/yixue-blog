package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Picture;
import com.yilin.yixueblog.model.entity.Subject;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.SubjectMapper;
import com.yilin.yixueblog.service.service.PictureService;
import com.yilin.yixueblog.service.service.SubjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.SubjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 专题表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2023-03-28
 */
@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {
    @Autowired
    private PictureService pictureService;
    /**
     * 获取专题列表
     * @param subjectVO
     * @return
     */
    @Override
    public IPage<Subject> getPageList(SubjectVO subjectVO) {
        QueryWrapper<Subject> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(subjectVO.getKeyword()) && !StringUtils.isEmpty(subjectVO.getKeyword().trim())) {
            queryWrapper.like("subject_name", subjectVO.getKeyword().trim());
        }
        Page<Subject> page = new Page<>();
        page.setCurrent(subjectVO.getCurrentPage());
        page.setSize(subjectVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("sort");
        IPage<Subject> pageList = baseMapper.selectPage(page, queryWrapper);
        List<Subject> list = pageList.getRecords();
        //封面uid集合
        List<String> fileList=new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileList.add(item.getFileUid());
            }
        });
        List<Picture> pictureList = null;
        Map<String, Picture> picMap = new HashMap<>();
        if (fileList.size()>0) {
            pictureList = pictureService.listByIds(fileList);
        }
        pictureList.forEach(item -> {
            picMap.put(item.getUid(), item);
        });
        for (Subject item : list) {
            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                item.setPicture(picMap.get(item.getFileUid()));
            }
        }
        pageList.setRecords(list);
        return pageList;
    }

}
