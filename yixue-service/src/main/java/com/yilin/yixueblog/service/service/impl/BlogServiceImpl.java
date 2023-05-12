package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yilin.yixueblog.model.entity.*;
import com.yilin.yixueblog.model.enums.ELevel;
import com.yilin.yixueblog.model.enums.EOriginal;
import com.yilin.yixueblog.model.enums.EPublish;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.BlogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.vo.BlogVO;
import com.yilin.yixueblog.service.service.*;
import com.yilin.yixueblog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 通过关键字搜索博客列表
     *
     * @param request
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Blog> getBlogByKeyword(HttpServletRequest request, Long currentPage, Long pageSize) {

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(request.getParameter("keywords"))) {
            String keyword = request.getParameter("keywords").trim();
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("summary", keyword));
        } else if (StringUtils.isNotEmpty(request.getParameter("tagUid"))) {
            String tagUid = request.getParameter("tagUid");
            addClickCount(tagUid);
            queryWrapper.like("tag_uid", tagUid);
        } else if (StringUtils.isNotEmpty(request.getParameter("blogSortUid"))) {
            String blogSortUid = request.getParameter("blogSortUid");
            addClickCount(blogSortUid);
            queryWrapper.eq("blog_sort_uid", blogSortUid);
        } else if (StringUtils.isNotEmpty(request.getParameter("author"))) {
            String author = request.getParameter("author");
            queryWrapper.eq("author", author);
        } else {
            return null;
        }

        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        //不要content字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        queryWrapper.orderByDesc("click_count");
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        IPage<Blog> iPage = blogService.page(page, queryWrapper);
        List<Blog> blogList = iPage.getRecords();
        //加工blog
        blogList = setBlog(blogList);

        iPage.setRecords(blogList);

        return iPage;
    }

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
        blogList = setBlog(blogList);
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
        blog.setMarkdown(blogVO.getMarkdown());
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
     *
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
            if (StringUtils.isNotEmpty(admin.getNickName())) {
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
        blog.setFileUid(blogVO.getPicture().getUid());
        blog.setLevel(blogVO.getLevel());
        blog.setIsOriginal(blogVO.getIsOriginal());
        blog.setIsPublish(blogVO.getIsPublish());
        blog.setOpenComment(blogVO.getOpenComment());
        blog.setType(blogVO.getType());
        blog.setOutsideLink(blogVO.getOutsideLink());
        blog.setStatus(EStatus.ENABLE);
        blog.setMarkdown(blogVO.getMarkdown());

        Boolean isSave = blog.updateById();
        return "修改成功";
    }

    /**
     * 删除blog（逻辑删除 修改状态为0）
     *
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
        if (b) {
            return "删除成功";
        }
        return "删除失败";
    }

    /**
     * 修改推荐博客
     *
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

        if (save) {
            return "拖拽成功";
        }
        return "拖拽失败";
    }

    /**
     * 通过推荐等级获取博客Page
     *
     * @param level       推荐级别
     * @param currentPage 当前页
     * @param useSort     是否使用排序字段
     * @return
     */
    @Override
    public List<Blog> getBlogPageByLevel(Integer level, Long currentPage, Integer useSort) {

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", level);
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        queryWrapper.orderByDesc("sort");

        List<Blog> blogList = baseMapper.selectList(queryWrapper);
        blogList = setBlog(blogList);

        return blogList;
    }

    /**
     * 通过Uid查找Blog
     *
     * @param uid
     * @return
     */
    @Override
    public Blog getBlogByUid(String uid) {
        Blog blog = baseMapper.selectById(uid);
        String tagUid = blog.getTagUid();
        List<String> tagUids = StringUtils.changeStringToString(tagUid, ",");

        BlogSort blogSort = blogSortService.getById(blog.getBlogSortUid());
        List<Tag> tags = tagService.listByIds(tagUids);
        Picture picture = pictureService.getById(blog.getFileUid());

        blog.setBlogSort(blogSort);
        blog.setTagList(tags);
        blog.setPicture(picture);
        blog.setCopyright("本文为易学博客原创文章，转载无需和我联系，但请注明来自易学博客");

        //给博客点击数增加
        Integer clickCount = blog.getClickCount() + 1;
        blog.setClickCount(clickCount);
        blog.updateById();

        return blog;
    }

    /**
     * 获取博客点击排行
     *
     * @return
     */
    @Override
    public List<Blog> getHotBlog() {
        //从Redis中获取内容

        //判断redis中是否有文章


        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(0);
        page.setSize(5);

        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        queryWrapper.orderByDesc("click_count");
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();
        list = setBlog(list);
        // 将从数据库查询的数据缓存到redis中[避免list中没有数据而保存至redis的情况]
//        if (list.size() > 0) {
//            redisUtil.setEx(RedisConf.HOT_BLOG, JsonUtils.objectToJson(list), 1, TimeUnit.HOURS);
//        }
        return list;
    }

    /**
     * 通过标签uid获取博客
     * @param tagUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Blog> getBlogByTagUid(String tagUid, Long currentPage, Long pageSize) {
        addClickCount(tagUid);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        queryWrapper.like("tag_uid", tagUid);
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        queryWrapper.orderByDesc("create_time");
        //排除博客内容
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        IPage<Blog> pageList = blogService.page(page, queryWrapper);
        List<Blog> list = pageList.getRecords();
        list = setBlog(list);
        pageList.setRecords(list);
        return pageList;
    }

    /**
     * 获取博客的归档日期
     *
     * @return
     */
    @Override
    public List getBlogTimeList() {
        //从Redis中获取内容
        String monthResult = redisUtil.get("MONTH_SET");
        //判断redis中时候包含归档的内容
        if (StringUtils.isNotEmpty(monthResult)) {
            List list = JsonUtils.jsonArrayToArrayList(monthResult);
            return list;
        }
        // 第一次启动的时候归档
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        List<Blog> list = blogService.list(queryWrapper);

        //给博客增加标签、分类、图片
        list = setBlog(list);

        Map<String, List<Blog>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Blog blog = (Blog) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Blog> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Blog> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);
            }
        }

        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            redisUtil.set("BLOG_SORT_BY_MONTH:" + key, JsonUtils.objectToJson(value).toString());
        });
        List<String> monthList = new ArrayList<String>(monthSet);
        //将从数据库查询的数据缓存到redis中
        redisUtil.set("MONTH_SET", JsonUtils.objectToJson(monthSet).toString());
        return monthList;
    }

    /**
     * 通过月份获取日期
     *
     * @param monthDate
     * @return
     */
    @Override
    public List<Blog> getBlogByMonth(String monthDate) {
        if (StringUtils.isEmpty(monthDate)) {
            return null;
        }
        //从Redis中获取内容
        String contentResult = redisUtil.get("BLOG_SORT_BY_MONTH:" + monthDate);

        //判断redis中时候包含该日期下的文章
        if (StringUtils.isNotEmpty(contentResult)) {
            List blogList = JsonUtils.jsonArrayToArrayList(contentResult);
            return blogList;
        }

        // 第一次启动的时候归档
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        List<Blog> list = blogService.list(queryWrapper);

        //给博客增加标签、分类、图片
        list = setBlog(list);

        Map<String, List<Blog>> map = new HashMap<>();
        Iterator iterable = list.iterator();
        Set<String> monthSet = new TreeSet<>();
        while (iterable.hasNext()) {
            Blog blog = (Blog) iterable.next();
            Date createTime = blog.getCreateTime();

            String month = new SimpleDateFormat("yyyy年MM月").format(createTime).toString();

            monthSet.add(month);

            if (map.get(month) == null) {
                List<Blog> blogList = new ArrayList<>();
                blogList.add(blog);
                map.put(month, blogList);
            } else {
                List<Blog> blogList = map.get(month);
                blogList.add(blog);
                map.put(month, blogList);
            }
        }
        // 缓存该月份下的所有文章  key: 月份   value：月份下的所有文章
        map.forEach((key, value) -> {
            redisUtil.set("BLOG_SORT_BY_MONTH:" + key, JsonUtils.objectToJson(value).toString());
        });
        //将从数据库查询的数据缓存到redis中
        redisUtil.set("MONTH_SET", JsonUtils.objectToJson(monthSet));
        return map.get(monthSet);
    }

    /**
     * 通过博客分类UID获取博客列表
     *
     * @param blogSortUid
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Blog> getBlogByBlogSortUid(String blogSortUid, Long currentPage, Long pageSize) {
        //添加点击数
        addClickCount(blogSortUid);
        //分页
        Page<Blog> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.eq("is_publish", EPublish.PUBLISH);
        queryWrapper.eq("blog_sort_uid", blogSortUid);

        //因为首页并不需要显示内容，所以需要排除掉内容字段
        queryWrapper.select(Blog.class, i -> !i.getProperty().equals("content"));
        IPage<Blog> pageList = blogService.page(page, queryWrapper);

        //给博客增加标签和分类
        List<Blog> list = setBlog(pageList.getRecords());
        pageList.setRecords(list);
        return pageList;
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

    /**
     * 加工blog
     *
     * @param blogList
     * @return
     */
    @Override
    public List<Blog> setBlog(List<Blog> blogList) {
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
        //图片uid
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
        return blogList;
    }

    /**
     * 通过博客uid获取相似博客
     *
     * @param blogUid
     * @return
     */
    @Override
    public List<Blog> getSimilarityBlogUid(String blogUid) {
        List<String> uids = null;
        try {
            uids = pythonBlog(blogUid);
            // System.out.println(uids);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (uids != null) {
            List<Blog> blogs = baseMapper.selectBatchIds(uids);
            return blogs;
        }

        return null;
    }

    /**
     * 添加高亮
     *
     * @param str
     * @param keyword
     * @return
     */
    private String getHitCode(String str, String keyword) {
        if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(str)) {
            return str;
        }
        String startStr = "<span style = 'color:red'>";
        String endStr = "</span>";
        // 判断关键字是否直接是搜索的内容，否者直接返回
        if (str.equals(keyword)) {
            return startStr + str + endStr;
        }
        String lowerCaseStr = str.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        String[] lowerCaseArray = lowerCaseStr.split(lowerKeyword);
        Boolean isEndWith = lowerCaseStr.endsWith(lowerKeyword);

        // 计算分割后的字符串位置
        Integer count = 0;
        List<Map<String, Integer>> list = new ArrayList<>();
        List<Map<String, Integer>> keyList = new ArrayList<>();
        for (int a = 0; a < lowerCaseArray.length; a++) {
            // 将切割出来的存储map
            Map<String, Integer> map = new HashMap<>();
            Map<String, Integer> keyMap = new HashMap<>();
            map.put("startIndex", count);
            Integer len = lowerCaseArray[a].length();
            count += len;
            map.put("endIndex", count);
            list.add(map);
            if (a < lowerCaseArray.length - 1 || isEndWith) {
                // 将keyword存储map
                keyMap.put("startIndex", count);
                count += keyword.length();
                keyMap.put("endIndex", count);
                keyList.add(keyMap);
            }
        }
        // 截取切割对象
        List<String> arrayList = new ArrayList<>();
        for (Map<String, Integer> item : list) {
            Integer start = item.get("startIndex");
            Integer end = item.get("endIndex");
            String itemStr = str.substring(start, end);
            arrayList.add(itemStr);
        }
        // 截取关键字
        List<String> keyArrayList = new ArrayList<>();
        for (Map<String, Integer> item : keyList) {
            Integer start = item.get("startIndex");
            Integer end = item.get("endIndex");
            String itemStr = str.substring(start, end);
            keyArrayList.add(itemStr);
        }

        StringBuffer sb = new StringBuffer();
        for (int a = 0; a < arrayList.size(); a++) {
            sb.append(arrayList.get(a));
            if (a < arrayList.size() - 1 || isEndWith) {
                sb.append(startStr);
                sb.append(keyArrayList.get(a));
                sb.append(endStr);
            }
        }
        return sb.toString();
    }

    /**
     * 调用python脚本
     *
     * @param blogUid
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static List<String> pythonBlog(String blogUid) throws IOException, InterruptedException {
        // 在Java中创建一个ProcessBuilder对象，指定python命令和脚本路径
        ProcessBuilder processBuilder = new ProcessBuilder("C:\\Users\\yilin\\Desktop\\python\\blog\\venv\\Scripts\\python.exe", "C:\\Users\\yilin\\Desktop\\python\\blog\\similarityBlog.py");
        // 设置参数列表，可以是任意类型和数量
        List<String> arguments = new ArrayList<>();
        arguments.add(blogUid);
        // 将参数列表添加到ProcessBuilder对象中
        processBuilder.command().addAll(arguments);
        // 启动进程并获取Process对象
        Process process = processBuilder.start();
        // 从进程的输入流中读取输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //错误流
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            List<String> list = (List<String>) JsonUtils.jsonArrayToArrayList(line, List.class);
            return list;
        }

        // 等待进程结束并获取退出码
        reader.close();
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        String err = null;
        while ((err = error.readLine()) != null) {
            System.out.println(err);
        }
        error.close();
        return null;
    }

    /**
     * 给分类或者标签添加点击数
     * @param uid
     */
    public void addClickCount(String uid) {
        BlogSort blogSort = blogSortService.getById(uid);
        Tag tag = tagService.getById(uid);
        HttpServletRequest request = RequestHolder.getRequest();
        String ip = IpUtils.getIpAddr(request);
        if (blogSort!=null){
            //从Redis取出数据，判断该用户24小时内，是否点击过该分类
            String jsonResult = redisUtil.get("SORT_CLICK:" + ip + "#" + uid);
            if (StringUtils.isEmpty(jsonResult)) {
                //给分类点击数增加
                int clickCount = blogSort.getClickCount() + 1;
                blogSort.setClickCount(clickCount);
                blogSort.updateById();
                //将该用户点击记录存储到redis中, 24小时后过期
                redisUtil.setEx("SORT_CLICK:" + ip + "#" + uid, clickCount + "",
                        24, TimeUnit.HOURS);
            }
        }
        if (tag != null ) {
            //从Redis取出数据，判断该用户24小时内，是否点击过该标签
            String jsonResult = redisUtil.get("TAG_CLICK:" + ip + "#" + uid);
            if (StringUtils.isEmpty(jsonResult)) {
                //给标签点击数增加
                int clickCount = tag.getClickCount() + 1;
                tag.setClickCount(clickCount);
                tag.updateById();
                //将该用户点击记录存储到redis中, 24小时后过期
                redisUtil.setEx("TAG_CLICK:" + ip + "#" + uid, clickCount + "",
                        24, TimeUnit.HOURS);
            }
        }

    }

}
