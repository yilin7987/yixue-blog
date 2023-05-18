package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.model.entity.SubjectItem;
import com.yilin.yixueblog.service.service.SubjectItemService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    private SubjectItemService subjectItemService;

    @Autowired
    @Lazy
    private SubjectService subjectService;
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
        if (list.size() == 0){
            return pageList;
        }
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

    /**
     * 批量删除
     * @param subjectVOList
     * @return
     */
    @Override
    public String deleteBatchSubject(List<SubjectVO> subjectVOList) {
        if (subjectVOList.size() == 0) {
            return "传入参数有误！";
        }
        List<String> uids = new ArrayList<>();
        subjectVOList.forEach(item -> {
            uids.add(item.getUid());
        });
        // 判断要删除的分类，是否有资源
        QueryWrapper<SubjectItem> subjectItemQueryWrapper = new QueryWrapper<>();
        subjectItemQueryWrapper.eq("status", EStatus.ENABLE);
        subjectItemQueryWrapper.in("subject_uid", uids);
        Integer count = Math.toIntExact(subjectItemService.count(subjectItemQueryWrapper));
        if (count > 0) {
            return "该专题下还有内容！";
        }
        Collection<Subject> subjectList = baseMapper.selectBatchIds(uids);
        subjectList.forEach(item -> {
            item.setUpdateTime(new Date());
            item.setStatus(EStatus.DISABLED);
        });
        Boolean save = subjectService.updateBatchById(subjectList);
        if (save) {
            return "删除成功";
        } else {
            return "删除失败";
        }
    }

    /**
     * 新增专题
     *
     * @param subjectVO
     */
    @Override
    public String addSubject(SubjectVO subjectVO) {
        /**
         * 判断需要增加的分类是否存在
         */
        QueryWrapper<Subject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("subject_name", subjectVO.getSubjectName());
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.last("LIMIT 1");
        Subject tempSubject = subjectService.getOne(queryWrapper);
        if (tempSubject != null) {
            return "该专题已存在";
        }
        Subject subject = new Subject();
        subject.setSubjectName(subjectVO.getSubjectName());
        subject.setSummary(subjectVO.getSummary());
        subject.setFileUid(subjectVO.getFileUid());
        subject.setClickCount(subjectVO.getClickCount());
        subject.setCollectCount(subjectVO.getCollectCount());
        subject.setSort(subjectVO.getSort());
        subject.setStatus(EStatus.ENABLE);
        subject.insert();
        return "添加成功";
    }

    /**
     * 编辑专题
     * @param subjectVO
     */
    @Override
    public String editSubject(SubjectVO subjectVO) {
        Subject subject = subjectService.getById(subjectVO.getUid());
        /**
         * 判断需要编辑的分类是否存在
         */
        if (!subject.getSubjectName().equals(subjectVO.getSubjectName())) {
            QueryWrapper<Subject> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("subject_name", subjectVO.getSubjectName());
            queryWrapper.eq("status", EStatus.ENABLE);
            Subject tempSubject = subjectService.getOne(queryWrapper);
            if (tempSubject != null) {
                return "该实体已存在";
            }
        }
        subject.setSubjectName(subjectVO.getSubjectName());
        subject.setSummary(subjectVO.getSummary());
        subject.setFileUid(subjectVO.getFileUid());
        subject.setClickCount(subjectVO.getClickCount());
        subject.setCollectCount(subjectVO.getCollectCount());
        subject.setSort(subjectVO.getSort());
        subject.setStatus(EStatus.ENABLE);
        subject.setUpdateTime(new Date());
        subject.updateById();
        return "更新成功";
    }
}
