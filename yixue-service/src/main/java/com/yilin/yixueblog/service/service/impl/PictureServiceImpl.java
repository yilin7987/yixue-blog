package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.utils.COSUtils;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Picture;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.PictureMapper;
import com.yilin.yixueblog.service.service.PictureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.PictureVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * <p>
 * 图片表 服务实现类
 * </p>
 *
 * @author yilin
 * @since 2022-11-17
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Autowired
    @Lazy
    private PictureService pictureService;
    @Autowired
    COSUtils cosUtils;

    /**
     * 通过分类uid查找图片
     *
     * @param sortUid
     * @return
     */
    @Override
    public List<Picture> getPictureBySort(String sortUid) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.in("pic_sort_uid", sortUid);

        List<Picture> picList = pictureService.list(queryWrapper);

        return picList;
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @Override
    public Picture uploadPicture(MultipartFile file) throws IOException {
        Picture picture = new Picture();
        //文件名字
        String originalFilename = file.getOriginalFilename();
        picture.setPicOldName(originalFilename);
        //后缀名
        String contentType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        picture.setPicExpandedName(contentType);
        //文件大小
        long size = file.getSize();
        picture.setPicSize(size);
        picture.setStatus(EStatus.ENABLE);

        picture.insert();


        String uploadStr;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();

            uploadStr = cosUtils.upload(inputStream, picture.getUid(), picture.getPicExpandedName());
            if (!"上传失败".equals(uploadStr)) {
                picture.setTxUrl(uploadStr);
                picture.setPicName(picture.getUid() + "." + picture.getPicExpandedName());
                picture.updateById();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
        return picture;
    }

    /**
     * 新增图片
     *
     * @param pictureVO
     * @return
     */
    @Override
    public String addPicture(PictureVO pictureVO) {
        Picture picture = baseMapper.selectById(pictureVO.getPicUid());
        picture.setPicSortUid(pictureVO.getPicSortUid());
        picture.updateById();
        return "添加成功";
    }

    /**
     * 获取图片列表
     *
     * @param pictureVO
     * @return
     */
    @Override
    public IPage<Picture> getPageList(PictureVO pictureVO) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(pictureVO.getKeyword()) && !StringUtils.isEmpty(pictureVO.getKeyword().trim())) {
            queryWrapper.like("pic_name", pictureVO.getKeyword().trim());
        }
        Page<Picture> page = new Page<>();
        page.setCurrent(pictureVO.getCurrentPage());
        page.setSize(pictureVO.getPageSize());
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("pic_sort_uid", pictureVO.getPicSortUid());
        queryWrapper.orderByDesc("create_time");
        IPage<Picture> pageList = pictureService.page(page, queryWrapper);

        return pageList;
    }

    /**
     * 删除图片
     * @param pictureUidList
     * @return
     */
    @Override
    public String deletePicture(List<String> pictureUidList) {

        if (pictureUidList.size() == 0) {
            return "参数有误";
        }
        List<Picture> pictures = pictureService.listByIds(pictureUidList);
        for (Picture picture : pictures) {
            picture.setStatus(EStatus.DISABLED);
        }
        pictureService.updateBatchById(pictures);
        return "删除成功";
    }


}
