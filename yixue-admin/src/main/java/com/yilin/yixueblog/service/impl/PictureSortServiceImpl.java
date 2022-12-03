package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Picture;
import com.yilin.yixueblog.entity.PictureSort;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.PictureSortMapper;
import com.yilin.yixueblog.service.PictureService;
import com.yilin.yixueblog.service.PictureSortService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.vo.PictureSortVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.awt.event.ItemEvent;
import java.util.*;

/**
 * <p>
 * 图片分类表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2022-11-20
 */
@Service
public class PictureSortServiceImpl extends ServiceImpl<PictureSortMapper, PictureSort> implements PictureSortService {
    @Autowired
    @Lazy
    private PictureSortService pictureSortService;

    @Autowired
    private PictureService pictureService;

    @Override
    public String addPictureSort(PictureSortVO pictureSortVO) {
        PictureSort pictureSort = new PictureSort();
        pictureSort.setName(pictureSortVO.getName());
        pictureSort.setParentUid(pictureSortVO.getParentUid());
        pictureSort.setSort(pictureSortVO.getSort());
        pictureSort.setFileUid(pictureSortVO.getFileUid());
        pictureSort.setStatus(EStatus.ENABLE);
        pictureSort.setIsShow(pictureSortVO.getIsShow());
        pictureSort.insert();
        return "添加成功";
    }

    /**
     * 获取图片分类列表
     *
     * @param pictureSortVO
     * @return
     */
    @Override
    public IPage<PictureSort> getPageList(PictureSortVO pictureSortVO) {
        QueryWrapper<PictureSort> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(pictureSortVO.getKeyword()) && !StringUtils.isEmpty(pictureSortVO.getKeyword().trim())) {
            queryWrapper.like("name", pictureSortVO.getKeyword().trim());
        }

        if (pictureSortVO.getIsShow() != null) {
            queryWrapper.eq("is_show", 1);
        }
        Page<PictureSort> page = new Page<>();
        page.setCurrent(pictureSortVO.getCurrentPage());
        page.setSize(pictureSortVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("sort");
        IPage<PictureSort> pageList = pictureSortService.page(page, queryWrapper);
        //分类集合
        List<PictureSort> pictureSortlist = pageList.getRecords();

        //分类封面Uid
        List<String> picUidList = new ArrayList<>();
        pictureSortlist.forEach(item -> {
            picUidList.add(item.getFileUid());
        });
        // 封面集合
        List<Picture> pictures = pictureService.listByIds(picUidList);
        //封面map （封面uid，图片实体类）
        Map<String, Picture> picMap = new HashMap<>();
        pictures.forEach(item -> {
            picMap.put(item.getUid(), item);
        });


        for (PictureSort pictureSort : pictureSortlist) {
            pictureSort.setPicture(picMap.get(pictureSort.getFileUid()));
        }

        pageList.setRecords(pictureSortlist);
        return pageList;
    }

    /**
     * 编辑分类
     *
     * @param pictureSortVO
     * @return
     */
    @Override
    public String editPictureSort(PictureSortVO pictureSortVO) {
        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        pictureSort.setName(pictureSortVO.getName());
        pictureSort.setSort(pictureSortVO.getSort());
        pictureSort.setFileUid(pictureSortVO.getFileUid());
        pictureSort.setIsShow(pictureSortVO.getIsShow());
        pictureSort.updateById();
        return "编辑成功";
    }

    /**
     * 删除图片分类
     *
     * @param pictureSortVO
     */
    @Override
    public String deletePictureSort(PictureSortVO pictureSortVO) {
        // 判断要删除的分类，是否有图片
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.eq("status", EStatus.ENABLE);
        pictureQueryWrapper.eq("pic_sort_uid", pictureSortVO.getUid());
        Integer pictureCount = Math.toIntExact(pictureService.count(pictureQueryWrapper));
        if (pictureCount > 0) {
            if (pictureSortVO.getIsDelete() != null && pictureSortVO.getIsDelete().intValue() == 1) {
                // 分类下的所有图片 逻辑删除
                UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("pic_sort_uid", pictureSortVO.getUid());
                updateWrapper.set("status", 0);
                pictureService.update(updateWrapper);
            } else {
                return "该分类还有图片";
            }
        }
        //删除分类
        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        pictureSort.setStatus(EStatus.DISABLED);
        pictureSort.updateById();
        return "删除成功";
    }
}
