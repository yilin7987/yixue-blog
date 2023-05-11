package com.yilin.yixueblog.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yilin.yixueblog.model.entity.PictureSort;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.model.vo.PictureSortVO;

/**
 * <p>
 * 图片分类表 服务类
 * </p>
 *
 * @author yilin
 * @since 2022-11-20
 */
public interface PictureSortService extends IService<PictureSort> {

    /**
     * 新增图片分类
     * @param pictureSortVO
     */
    String addPictureSort(PictureSortVO pictureSortVO);

    /**
     * 获取图片分类列表
     * @param pictureSortVO
     * @return
     */
    IPage<PictureSort> getPageList(PictureSortVO pictureSortVO);

    /**
     * 编辑分类
     * @param pictureSortVO
     * @return
     */
    String editPictureSort(PictureSortVO pictureSortVO);

    /**
     * 删除图片分类
     * @param pictureSortVO
     */
    String deletePictureSort(PictureSortVO pictureSortVO);
}
