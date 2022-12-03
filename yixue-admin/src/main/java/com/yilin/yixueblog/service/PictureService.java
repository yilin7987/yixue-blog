package com.yilin.yixueblog.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yilin.yixueblog.entity.Picture;
import com.yilin.yixueblog.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 图片表 服务类
 * </p>
 *
 * @author yilin
 * @since 2022-11-17
 */
public interface PictureService extends IService<Picture> {

    /**
     * 通过分类uid查找图片
     * @param sortUid
     * @return
     */
    List<Picture> getPictureBySort(String sortUid);

    /**
     * 上传图片
     * @param file
     * @return
     */
    Picture uploadPicture(MultipartFile file) throws IOException;

    /**
     * 新增图片
     * @param pictureVO
     * @return
     */
    String addPicture(PictureVO pictureVO);

    /**
     * 获取图片列表
     * @param pictureVO
     * @return
     */
    IPage<Picture> getPageList(PictureVO pictureVO);

    /**
     * 删除图片
     * @param pictureUidList
     * @return
     */
    String deletePicture(List<String> pictureUidList);
}
