package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.Blog;
import com.yilin.yixueblog.entity.BlogSort;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.BlogMapper;
import com.yilin.yixueblog.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.vo.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    @Autowired
    private BlogService blogService;
    /**
     * 获取博客列表
     * @param blogVO
     * @return
     */
    @Override
    public IPage<Blog> getPageList(BlogVO blogVO) {
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        // 构建搜索条件
        if (StringUtils.isNotEmpty(blogVO.getKeyword()) && !StringUtils.isEmpty(blogVO.getKeyword().trim())) {
            queryWrapper.like("title", blogVO.getKeyword().trim());
        }
        if (!StringUtils.isEmpty(blogVO.getTagUid())) {
            queryWrapper.like("tag_uid", blogVO.getTagUid());
        }
        if (!StringUtils.isEmpty(blogVO.getBlogSortUid())) {
            queryWrapper.like("blog_sort_uid", blogVO.getBlogSortUid());
        }
        if (!StringUtils.isEmpty(blogVO.getLevelKeyword())) {
            queryWrapper.eq("level", blogVO.getLevelKeyword());
        }
        if (!StringUtils.isEmpty(blogVO.getIsPublish())) {
            queryWrapper.eq("is_publish", blogVO.getIsPublish());
        }
        if (!StringUtils.isEmpty(blogVO.getIsOriginal())) {
            queryWrapper.eq("is_original", blogVO.getIsOriginal());
        }
        if(!StringUtils.isEmpty(blogVO.getType())) {
            queryWrapper.eq("type", blogVO.getType());
        }

        //分页
        Page<Blog> page = new Page<>();
        page.setCurrent(blogVO.getCurrentPage());
        page.setSize(blogVO.getPageSize());
        //状态 ： 1激活
        queryWrapper.eq("status", EStatus.ENABLE);

        if(StringUtils.isNotEmpty(blogVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(blogVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        }else if(StringUtils.isNotEmpty(blogVO.getOrderByDescColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(blogVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            // 是否启动排序字段
            if (blogVO.getUseSort() == 0) {
                // 未使用，默认按时间倒序
                queryWrapper.orderByDesc("create_time");
            } else {
                // 使用，默认按sort值大小倒序
                queryWrapper.orderByDesc("sort");
            }
        }

        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();

        if (list.size() == 0) {
            return pageList;
        }

        final StringBuffer fileUids = new StringBuffer();
        //分类uid
        List<String> sortUids = new ArrayList<>();
        //标签uid
        List<String> tagUids = new ArrayList<>();

            list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + ",");
            }
            if (StringUtils.isNotEmpty(item.getBlogSortUid())) {
                sortUids.add(item.getBlogSortUid());
            }
            //数据库存储形式： taguid,taguid,taguid  一个blog可能有多个tag
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), ",");
                for (String itemTagUid : tagUidsTemp) {
                    tagUids.add(itemTagUid);
                }
            }
        });
//        String pictureList = null;
//        if (fileUids != null) {
//            pictureList = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
//        }
//
//        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureList);
//        Collection<BlogSort> sortList = new ArrayList<>();
//        Collection<Tag> tagList = new ArrayList<>();
//
//        if (sortUids.size() > 0) {
//            sortList = blogSortService.listByIds(sortUids);
//        }
//        if (tagUids.size() > 0) {
//            tagList = tagService.listByIds(tagUids);
//        }
//
//
//        Map<String, BlogSort> sortMap = new HashMap<>();
//        Map<String, Tag> tagMap = new HashMap<>();
//        Map<String, String> pictureMap = new HashMap<>();
//
//        sortList.forEach(item -> {
//            sortMap.put(item.getUid(), item);
//        });
//
//        tagList.forEach(item -> {
//            tagMap.put(item.getUid(), item);
//        });
//
//        picList.forEach(item -> {
//            pictureMap.put(item.get(SQLConf.UID).toString(), item.get(SQLConf.URL).toString());
//        });
//
//
//        for (Blog item : list) {
//
//            //设置分类
//            if (StringUtils.isNotEmpty(item.getBlogSortUid())) {
//                item.setBlogSort(sortMap.get(item.getBlogSortUid()));
//            }
//
//            //获取标签
//            if (StringUtils.isNotEmpty(item.getTagUid())) {
//                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), SysConf.FILE_SEGMENTATION);
//                List<Tag> tagListTemp = new ArrayList<Tag>();
//
//                tagUidsTemp.forEach(tag -> {
//                    tagListTemp.add(tagMap.get(tag));
//                });
//                item.setTagList(tagListTemp);
//            }
//
//            //获取图片
//            if (StringUtils.isNotEmpty(item.getFileUid())) {
//                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
//                List<String> pictureListTemp = new ArrayList<>();
//
//                pictureUidsTemp.forEach(picture -> {
//                    pictureListTemp.add(pictureMap.get(picture));
//                });
//                item.setPhotoList(pictureListTemp);
//            }
//        }
//        pageList.setRecords(list);
        return pageList;
    }
}
