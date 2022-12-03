package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.RequestHolder;
import com.yilin.yixueblog.Result;
import com.yilin.yixueblog.StringUtils;
import com.yilin.yixueblog.entity.*;
import com.yilin.yixueblog.enums.ELevel;
import com.yilin.yixueblog.enums.EOriginal;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.BlogMapper;
import com.yilin.yixueblog.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.vo.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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
    @Lazy
    private BlogService blogService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private TagService tagService;
    @Autowired
    private BlogSortService blogSortService;
    @Autowired
    private AdminService adminService;

    /**
     * 获取博客列表
     *
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
        //将List<Tag>转换成字符串
        String tagUid = changeListToString(blogVO.getTagList());
        if (!StringUtils.isEmpty(tagUid)) {
            queryWrapper.like("tag_uid", tagUid);
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
        if (!StringUtils.isEmpty(blogVO.getType())) {
            queryWrapper.eq("type", blogVO.getType());
        }

        //分页
        Page<Blog> page = new Page<>();
        page.setCurrent(blogVO.getCurrentPage());
        page.setSize(blogVO.getPageSize());
        //状态 ： 1激活
        queryWrapper.eq("status", EStatus.ENABLE);

        if (StringUtils.isNotEmpty(blogVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(blogVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        } else if (StringUtils.isNotEmpty(blogVO.getOrderByDescColumn())) {
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
        List<Blog> blogList = pageList.getRecords();

        if (blogList.size() == 0) {
            return pageList;
        }
        //图片
        List<String> picUids = new ArrayList<>();
        //分类uid
        List<String> sortUids = new ArrayList<>();
        //标签uid
        List<String> tagUids = new ArrayList<>();
        // 将 图片，blog分类，tag 分类存储成List
        blogList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                picUids.add(item.getFileUid());
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


        List<Picture> picList = new ArrayList<>();
        List<BlogSort> sortList = new ArrayList<>();
        List<Tag> tagList = new ArrayList<>();
        if (picUids.size() > 0) {
            picList = pictureService.listByIds(picUids);
        }
        if (sortUids.size() > 0) {
            sortList = blogSortService.listByIds(sortUids);
        }
        if (tagUids.size() > 0) {
            tagList = tagService.listByIds(tagUids);
        }
        // 加工List
        Map<String, Picture> picMap = new HashMap<>();
        Map<String, BlogSort> sortMap = new HashMap<>();
        Map<String, Tag> tagMap = new HashMap<>();
        picList.forEach(item -> {
            picMap.put(item.getUid(), item);
        });
        sortList.forEach(item -> {
            sortMap.put(item.getUid(), item);
        });
        tagList.forEach(item -> {
            tagMap.put(item.getUid(), item);
        });

        for (Blog item : blogList) {
            //设置分类
            if (StringUtils.isNotEmpty(item.getBlogSortUid())) {
                item.setBlogSort(sortMap.get(item.getBlogSortUid()));
            }
            //设置标签
            if (StringUtils.isNotEmpty(item.getTagUid())) {
                List<String> tagUidsTemp = StringUtils.changeStringToString(item.getTagUid(), ",");
                List<Tag> tagListTemp = new ArrayList<>();

                tagUidsTemp.forEach(tag -> {
                    tagListTemp.add(tagMap.get(tag));
                });
                item.setTagList(tagListTemp);
            }
            //设置图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                Picture pictureTemp = null;
                pictureTemp = picMap.get(item.getFileUid());
                item.setPicture(pictureTemp);
            }
        }
        pageList.setRecords(blogList);
        return pageList;
    }

    /**
     * 添加博客
     *
     * @param blogVO
     * @return
     */
    @Override
    public String addBlog(BlogVO blogVO) {
        HttpServletRequest request = RequestHolder.getRequest();
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", blogVO.getLevel());
        queryWrapper.eq("status", EStatus.ENABLE);
        Integer count = Math.toIntExact(blogService.count(queryWrapper));

        // 判断插入博客的时候，会不会超过预期设置
        String addVerdictResult = addVerdict(count + 1, blogVO.getLevel());
        // 判断是否能够添加推荐
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }
        Blog blog = new Blog();
        //如果是原创，作者为用户的昵称
        request.setAttribute("adminUid", "1");
        if (EOriginal.ORIGINAL.equals(blogVO.getIsOriginal())) {
            Admin admin = adminService.getById(request.getAttribute("adminUid").toString());
            if (admin != null) {
                if (StringUtils.isNotEmpty(admin.getNickName())) {
                    //作者名字
                    blog.setAuthor(admin.getNickName());
                } else {
                    blog.setAuthor(admin.getUserName());
                }
                blog.setAdminUid(admin.getUid());
            }
            //文章出处
            blog.setArticlesPart("admin");
        } else {
            blog.setAuthor(blogVO.getAuthor());
            blog.setArticlesPart(blogVO.getArticlesPart());
        }
        //将List<Tag>转换成字符串
        String tagUid = changeListToString(blogVO.getTagList());

        blog.setTitle(blogVO.getTitle());
        blog.setSummary(blogVO.getSummary());
        blog.setContent(blogVO.getContent());
        blog.setTagUid(tagUid);
        blog.setBlogSortUid(blogVO.getBlogSortUid());
        if (blogVO.getPicture() != null) {
            blog.setFileUid(blogVO.getPicture().getUid());
        }
        blog.setLevel(blogVO.getLevel());
        blog.setIsOriginal(blogVO.getIsOriginal());
        blog.setIsPublish(blogVO.getIsPublish());
        blog.setType(blogVO.getType());
        blog.setOutsideLink(blogVO.getOutsideLink());
        blog.setStatus(EStatus.ENABLE);
        blog.setOpenComment(blogVO.getOpenComment());
        Boolean isSave = blogService.save(blog);

        //保存成功后，需要发送消息到solr 和 redis
//        updateSolrAndRedis(isSave, blog);
        return "添加成功";
    }

    /**
     * 编辑博客
     * @param blogVO
     */
    @Override
    public String editBlog(BlogVO blogVO) {
        Blog blog = blogService.getById(blogVO.getUid());
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", blogVO.getLevel());
        queryWrapper.eq("status", EStatus.ENABLE);
        Integer count = Math.toIntExact(blogService.count(queryWrapper));
        if (blog != null) {
            //传递过来的和数据库中的不同，代表用户已经修改过等级了，那么需要将count数加1
            if (!blog.getLevel().equals(blogVO.getLevel())) {
                count += 1;
            }
        }
        String addVerdictResult = addVerdict(count, blogVO.getLevel());
        //添加的时候进行判断
        if (StringUtils.isNotBlank(addVerdictResult)) {
            return addVerdictResult;
        }
        //如果是原创，作者为用户的昵称
        Admin admin = adminService.getById("1");
        blog.setAdminUid(admin.getUid());
        if (EOriginal.ORIGINAL.equals(blogVO.getIsOriginal())) {
            if(StringUtils.isNotEmpty(admin.getNickName())) {
                blog.setAuthor(admin.getNickName());
            } else {
                blog.setAuthor(admin.getUserName());
            }
            blog.setArticlesPart("admin");
        } else {
            blog.setAuthor(blogVO.getAuthor());
            blog.setArticlesPart(blogVO.getArticlesPart());
        }

        blog.setTitle(blogVO.getTitle());
        blog.setSummary(blogVO.getSummary());
        blog.setContent(blogVO.getContent());

        //将List<Tag>转换成字符串
        String tagUid = changeListToString(blogVO.getTagList());
        blog.setTagUid(tagUid);

        blog.setBlogSortUid(blogVO.getBlogSortUid());
        blog.setFileUid(blogVO.getFileUid());
        blog.setLevel(blogVO.getLevel());
        blog.setIsOriginal(blogVO.getIsOriginal());
        blog.setIsPublish(blogVO.getIsPublish());
        blog.setOpenComment(blogVO.getOpenComment());
        blog.setType(blogVO.getType());
        blog.setOutsideLink(blogVO.getOutsideLink());
        blog.setStatus(EStatus.ENABLE);

        Boolean isSave = blog.updateById();
        return "修改成功";
    }

    /**
     * 删除blog（逻辑删除 修改状态为0）
     * @param blogUidList
     * @return
     */
    @Override
    public String deleteBlog(List<String> blogUidList) {
        List<Blog> blogList = blogService.listByIds(blogUidList);
        blogList.forEach(item -> {
            item.setStatus(EStatus.DISABLED);
        });
        boolean b = blogService.updateBatchById(blogList);
        if(b){
            return "删除成功";
        }
        return "删除失败";
    }

    /**
     * 修改推荐博客
     * @param blogVOList
     * @return
     */
    @Override
    public String editRecommendBlog(List<BlogVO> blogVOList) {
        if (blogVOList.size() <= 0) {
            return "参数错误";
        }
        List<String> blogUidList = new ArrayList<>();
        Map<String, BlogVO> blogVOMap = new HashMap<>();
        blogVOList.forEach(item -> {
            blogUidList.add(item.getUid());
            blogVOMap.put(item.getUid(), item);
        });

        List<Blog> blogList = blogService.listByIds(blogUidList);
        blogList.forEach(blog -> {
            BlogVO blogVO = blogVOMap.get(blog.getUid());
            if (blogVO != null) {
                blog.setSort(blogVO.getSort());
            }
        });
        Boolean save = blogService.updateBatchById(blogList);

        if (save){
            return "拖拽成功";
        }
        return "拖拽失败";
    }

    /**
     * 获取TagUid字符串
     *
     * @param list
     * @return
     */
    public static String changeListToString(List<Tag> list) {
        if (list == null) {
            return null;
        }
        final StringBuffer str = new StringBuffer();
        for (Tag tag : list) {
            str.append(tag.getUid() + ",");
        }
        if (str.length() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    /**
     * 添加时校验
     *
     * @param count
     * @param level
     * @return
     */
    private String addVerdict(Integer count, Integer level) {

        //添加的时候进行判断
        switch (level) {
            case ELevel.FIRST: {
//                Long blogFirstCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FIRST_COUNT));
                if (count > 5) {
                    return "一级推荐不能超过" + 5 + "个";
                }
            }
            break;

            case ELevel.SECOND: {
//                Long blogSecondCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_SECOND_COUNT));
                if (count > 2) {
                    return "二级推荐不能超过" + 2 + "个";
                }
            }
            break;

            case ELevel.THIRD: {
//                Long blogThirdCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_THIRD_COUNT));
                if (count > 3) {
                    return "三级推荐不能超过" + 3 + "个";
                }
            }
            break;

            case ELevel.FOURTH: {
//                Long blogFourthCount = Long.valueOf(sysParamsService.getSysParamsValueByKey(SysConf.BLOG_FOURTH_COUNT));
                if (count > 5) {
                    return "四级推荐不能超过" + 5 + "个";
                }
            }
            break;
            default: {

            }
        }
        return null;
    }
}
