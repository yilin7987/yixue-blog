package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.utils.RedisUtil;
import com.yilin.yixueblog.utils.RequestHolder;
import com.yilin.yixueblog.utils.Result;
import com.yilin.yixueblog.utils.StringUtils;
import com.yilin.yixueblog.model.entity.Avatar;
import com.yilin.yixueblog.model.entity.Blog;
import com.yilin.yixueblog.model.entity.Comment;
import com.yilin.yixueblog.model.entity.User;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.CommentMapper;
import com.yilin.yixueblog.service.service.AvatarService;
import com.yilin.yixueblog.service.service.BlogService;
import com.yilin.yixueblog.service.service.CommentService;
import com.yilin.yixueblog.service.service.UserService;
import com.yilin.yixueblog.model.vo.CommentVO;
import com.yilin.yixueblog.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private BlogService blogService;

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取评论数目
     * @param status
     */
    @Override
    public Integer getCommentCount(int status) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }

    @Override
    public String add(CommentVO commentVO) {
        // 判断该博客是否开启评论功能
//        if (StringUtils.isNotEmpty(commentVO.getBlogUid())) {
//            Blog blog = blogService.getById(commentVO.getBlogUid());
//            if (SysConf.CAN_NOT_COMMENT.equals(blog.getOpenComment())) {
//                return ResultUtil.result(SysConf.ERROR, MessageConf.BLOG_NO_OPEN_COMMENTS);
//            }
//        }
        HttpServletRequest request = RequestHolder.getRequest();
        String userUid = request.getAttribute("userUid").toString();
        User user = userService.getById(userUid);
        // 判断字数是否超过限制
        if (commentVO.getContent().length() > 1024) {
            return "评论不能超过1024个字符";
        }

        Comment comment = new Comment();
        //设置博客uid
        comment.setBlogUid(commentVO.getBlogUid());
        //设置内容
        comment.setContent(commentVO.getContent());
        // 当该评论不是一级评论时，需要设置一级评论UID字段
        if (StringUtils.isNotEmpty(commentVO.getToUid())) {
            //设置回复用户的uid
            comment.setToUserUid(commentVO.getToUserUid());
            Comment toComment = baseMapper.selectById(commentVO.getToUid());
            // 表示 toComment是非一级评论
            if (toComment != null && StringUtils.isNotEmpty(toComment.getFirstCommentUid())) {
                comment.setFirstCommentUid(toComment.getFirstCommentUid());
            } else {
                // 表示父评论是一级评论，直接获取UID
                comment.setFirstCommentUid(toComment.getUid());
            }
        }

        comment.setUserUid(commentVO.getUserUid());
        comment.setToUid(commentVO.getToUid());
        comment.setStatus(EStatus.ENABLE);
        comment.setSource("");
        comment.insert();

        //获取图片
//        if (StringUtils.isNotEmpty(user.getAvatar())) {
//            Avatar avatar= avatarService.getById(user.getAvatar());
//            if (avatar!=null) {
//                user.setPhotoUrl(avatar.getAvatarUrl());
//            }
//        }
//        comment.setUser(user);

        // 如果是回复某人的评论，那么需要向该用户Redis收件箱中中写入一条记录
        if (StringUtils.isNotEmpty(comment.getToUserUid())) {
            String redisKey = "USER_RECEIVE_COMMENT_COUNT:" + comment.getToUserUid();
            String count = redisUtil.get(redisKey);
            if (StringUtils.isNotEmpty(count)) {
                redisUtil.incrBy(redisKey, 1);
            } else {
                redisUtil.setEx(redisKey, "1", 7, TimeUnit.DAYS);
            }
        }
        return "成功";
    }

    /**
     * 获取评论列表
     *
     * @param commentVO
     * @return
     */
    @Override
    public IPage<Comment> getList(CommentVO commentVO) {
        //要返回的评论列表
        List<Comment> resultList = new ArrayList<>();

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("blog_uid", commentVO.getBlogUid());
        //分页
        Page<Comment> page = new Page<>();
        page.setCurrent(commentVO.getCurrentPage());
        page.setSize(commentVO.getPageSize());
        queryWrapper.eq("type", 0);
        queryWrapper.eq("status", EStatus.ENABLE);
        //一级评论没有toUid
        queryWrapper.isNull("to_uid");
        queryWrapper.orderByDesc("create_time");
        // 查询出所有的一级评论，进行分页显示
        IPage<Comment> pageList = baseMapper.selectPage(page, queryWrapper);
        //一级评论列表
        List<Comment> firstlist = pageList.getRecords();
        //全部评论列表
        List<Comment> allList = new ArrayList<>(firstlist);

        //一级评论的uid
        List<String> firstUidList = new ArrayList<>();
        firstlist.forEach(item -> {
            firstUidList.add(item.getUid());
        });

        List<Comment> notFirstList = new ArrayList<>();
        if (firstUidList.size() > 0) {
            // 查询一级评论下的子评论
            QueryWrapper<Comment> notFirstQueryWrapper = new QueryWrapper<>();
            notFirstQueryWrapper.in("first_comment_uid", firstUidList);
            notFirstQueryWrapper.eq("status", EStatus.ENABLE);
            notFirstQueryWrapper.orderByAsc("create_time");
            notFirstList = baseMapper.selectList(notFirstQueryWrapper);
            // 将子评论加入总的评论中
            if (notFirstList.size() > 0) {
                allList.addAll(notFirstList);
            }
        }
        //用户uid
        List<String> userUidList = new ArrayList<>();
        allList.forEach(item -> {
            String userUid = item.getUserUid();
            String toUserUid = item.getToUserUid();
            if (StringUtils.isNotEmpty(userUid)) {
                userUidList.add(item.getUserUid());
            }
            if (StringUtils.isNotEmpty(toUserUid)) {
                userUidList.add(item.getToUserUid());
            }
        });
        Collection<User> userList = new ArrayList<>();
        if (userUidList.size() > 0) {
            userList = userService.listByIds(userUidList);
        }

        // 过滤掉用户的敏感信息
        List<User> filterUserList = new ArrayList<>();
        List<String> avatarUidList = new ArrayList<>();
        userList.forEach(item -> {
            User user = new User();
            user.setAvatar(item.getAvatar());
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                avatarUidList.add(item.getAvatar());
            }
            user.setUid(item.getUid());
            user.setNickName(item.getNickName());
            user.setUserTag(item.getUserTag());
            filterUserList.add(user);
        });

        List<Avatar> avatarList = new ArrayList<>();
        if (avatarUidList.size() > 0) {
            // 获取用户头像
            avatarList = avatarService.listByIds(avatarUidList);
        }
        // 头像uid ， 头像实体
        Map<String, Avatar> avatarMap = new HashMap<>();
        avatarList.forEach(item -> {
            avatarMap.put(item.getUid(), item);
        });

        Map<String, User> userMap = new HashMap<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar()) && avatarMap.get(item.getAvatar()) != null) {
                item.setPhotoUrl(avatarMap.get(item.getAvatar()).getAvatarUrl());
            }
            userMap.put(item.getUid(), item);
        });

        Map<String, Comment> commentMap = new HashMap<>();
        allList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getUserUid())) {
                item.setUser(userMap.get(item.getUserUid()));
            }
            if (StringUtils.isNotEmpty(item.getToUserUid())) {
                item.setToUser(userMap.get(item.getToUserUid()));
            }
            commentMap.put(item.getUid(), item);
        });

        // 设置一级评论下的子评论
        for (Comment firstComment : firstlist) {
            List<Comment> temp = new ArrayList<>();
            for (Comment comment : notFirstList) {
                if (firstComment.getUid().equals(comment.getFirstCommentUid())) {
                    temp.add(comment);
                }
            }
            firstComment.setReplyList(temp);
        }

        pageList.setRecords(firstlist);
        return pageList;
    }

    /**
     * 获取用户的评论
     *
     * @param userVO
     * @return
     */
    @Override
    public Map<String, Object> getListByUser(HttpServletRequest request, UserVO userVO) {
        if (request.getAttribute("userUid") == null) {
            return null;
        }
        String requestUserUid = request.getAttribute("userUid").toString();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

        //分页
        Page<Comment> page = new Page<>();
        page.setCurrent(userVO.getCurrentPage());
        page.setSize(userVO.getPageSize());
        queryWrapper.eq("type", 0);
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.orderByDesc("create_time");
        // 查找出 我的评论 和 我的回复
        queryWrapper.and(wrapper -> wrapper.eq("user_uid", requestUserUid).or().eq("to_user_uid", requestUserUid));
        IPage<Comment> pageList = baseMapper.selectPage(page, queryWrapper);
        List<Comment> list = pageList.getRecords();
        // 获取用户id
        List<String> userUidList = new ArrayList<>();
        list.forEach(item -> {
            String userUid = item.getUserUid();
            String toUserUid = item.getToUserUid();
            if (StringUtils.isNotEmpty(userUid)) {
                userUidList.add(item.getUserUid());
            }
            if (StringUtils.isNotEmpty(toUserUid)) {
                userUidList.add(item.getToUserUid());
            }
        });

        // 获取用户列表
        Collection<User> userList = new ArrayList<>();
        if (userUidList.size() > 0) {
            userList = userService.listByIds(userUidList);
        }
        // 过滤掉用户的敏感信息
        List<User> filterUserList = new ArrayList<>();
        userList.forEach(item -> {
            User user = new User();
            user.setAvatar(item.getAvatar());
            user.setUid(item.getUid());
            user.setNickName(item.getNickName());
            filterUserList.add(user);
        });
        // 获取用户头像
        List<String> fileUids = new ArrayList<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                fileUids.add(item.getAvatar());
            }
        });

        List<Avatar> avatarList = avatarService.listByIds(fileUids);

        Map<String, Avatar> avatarMap = new HashMap<>();
        if (avatarList.size() > 0) {
            avatarList.forEach(item -> {
                avatarMap.put(item.getUid(), item);
            });
        }

        Map<String, User> userMap = new HashMap<>();
        filterUserList.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar()) && avatarMap.get(item.getAvatar()) != null) {
                item.setPhotoUrl(avatarMap.get(item.getAvatar()).getAvatarUrl());
            }
            userMap.put(item.getUid(), item);
        });


        // 将评论列表划分为 我的评论 和 我的回复
        List<Comment> commentList = new ArrayList<>();
        List<Comment> replyList = new ArrayList<>();
        list.forEach(item -> {
            //设置用户
            if (StringUtils.isNotEmpty(item.getUserUid())) {
                item.setUser(userMap.get(item.getUserUid()));
            }

            if (StringUtils.isNotEmpty(item.getToUserUid())) {
                item.setToUser(userMap.get(item.getToUserUid()));
            }

            if (requestUserUid.equals(item.getUserUid())) {
                commentList.add(item);
            }
            if (requestUserUid.equals(item.getToUserUid())) {
                replyList.add(item);
            }
        });

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("commentList", commentList);
        resultMap.put("replyList", replyList);
        return resultMap;
    }

    /**
     * 获取用户点赞
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<Comment> getPraiseListByUser(Long currentPage, Long pageSize) {
        HttpServletRequest request = RequestHolder.getRequest();
        if (request.getAttribute("userUid") == null || request.getAttribute("token") == null) {
            return null;
        }
        String userUid = request.getAttribute("userUid").toString();
        QueryWrapper<Comment> queryWrappe = new QueryWrapper<>();
        queryWrappe.eq("user_uid", userUid);
        queryWrappe.eq("type", 1);
        queryWrappe.eq("status", EStatus.ENABLE);
        queryWrappe.orderByDesc("create_time");
        Page<Comment> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        IPage<Comment> pageList = baseMapper.selectPage(page, queryWrappe);
        List<Comment> praiseList = pageList.getRecords();

        List<String> blogUids = new ArrayList<>();
        praiseList.forEach(item -> {
            blogUids.add(item.getBlogUid());
        });
        Map<String, Blog> blogMap = new HashMap<>();
        if (blogUids.size() > 0) {
            Collection<Blog> blogList = blogService.listByIds(blogUids);
            blogList.forEach(blog -> {
                // 并不需要content内容
                blog.setContent("");
                blogMap.put(blog.getUid(), blog);
            });
        }

        praiseList.forEach(item -> {
            if (blogMap.get(item.getBlogUid()) != null) {
                item.setBlog(blogMap.get(item.getBlogUid()));
            }
        });
        pageList.setRecords(praiseList);
        return pageList;
    }

    @Override
    public Result praiseBlogByUid(String blogUid) {
        HttpServletRequest request = RequestHolder.getRequest();
        String userUid = null;
        if (request != null) {
            userUid = request.getAttribute("userUid").toString();
        }

        // 如果用户登录了
        if (userUid != null) {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_uid", userUid);
            queryWrapper.eq("blog_uid", blogUid);
            queryWrapper.eq("type", 1);
            queryWrapper.last("LIMIT 1");
            Comment praise = baseMapper.selectOne(queryWrapper);
            if (praise != null) {
                return Result.err().message("您已经点赞过了!");
            }
        } else {
            return Result.err().message("请先登录后才能点赞!");
        }
        Blog blog = blogService.getById(blogUid);
        //给该博客点赞 +1
        Integer count = blog.getCollectCount() + 1;
        //放入redis
        redisUtil.set("BLOG_PRAISE:" + blogUid, count.toString(),24,TimeUnit.HOURS);
        blog.setCollectCount(count);
        blog.updateById();
        //向评论表添加点赞数据
        Comment comment = new Comment();
        comment.setUserUid(userUid);
        comment.setBlogUid(blogUid);
        comment.setType(1);
        comment.setSource("");
        comment.insert();
        return Result.succeed().data(blog.getCollectCount());
    }

    /**
     * 判定用户是否点赞
     *
     * @param blogUid
     * @return
     */
    @Override
    public String isPraise(String blogUid) {
        HttpServletRequest request = RequestHolder.getRequest();
        // 如果用户登录了
        if (request.getAttribute("userUid") != null) {
            String userUid = request.getAttribute("userUid").toString();
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_uid", userUid);
            queryWrapper.eq("blog_uid", blogUid);
            queryWrapper.eq("type", 1);
            queryWrapper.last("LIMIT 1");
            Comment praise = baseMapper.selectOne(queryWrapper);
            if (praise != null) {
                return "已点赞";
            }
            return "未点赞";
        }
        return null;
    }

}
