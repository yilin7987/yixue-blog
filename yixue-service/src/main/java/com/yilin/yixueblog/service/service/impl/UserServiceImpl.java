package com.yilin.yixueblog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.model.entity.Avatar;
import com.yilin.yixueblog.model.entity.User;
import com.yilin.yixueblog.model.enums.EStatus;
import com.yilin.yixueblog.service.mapper.UserMapper;
import com.yilin.yixueblog.service.service.AvatarService;
import com.yilin.yixueblog.service.service.UserService;
import com.yilin.yixueblog.model.vo.UserVO;
import com.yilin.yixueblog.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Lazy
    private AvatarService avatarService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    @Lazy
    private UserService userService;

    /**
     * 邮件发件人
     */
    @Value("${mail.fromMail.fromAddress}")
    private String fromAddress;

    /**
     * 获取用户数
     * @param status
     * @return
     */
    @Override
    public Integer getUserCount(int status) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }

    /**
     * 登录功能
     * @param userVO
     * @return
     */
    @Override
    public String login(UserVO userVO) {
        String userName = userVO.getUserName();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq("user_name", userName).or().eq("email", userName));
        queryWrapper.last("LIMIT 1");
        User user = baseMapper.selectOne(queryWrapper);
        if (user == null || EStatus.DISABLED == user.getStatus()) {
            return "-1";
        }

        if (StringUtils.isNotEmpty(user.getPassWord()) && user.getPassWord().equals(MD5Utils.string2MD5(userVO.getPassWord()))) {
            // 更新登录信息
            HttpServletRequest request = RequestHolder.getRequest();
            user.setLastLoginIp(IpUtils.getIpAddr(request));
            user.setLastLoginTime(new Date());
            // 登录成功后，次数+1
            user.setLoginCount(user.getLoginCount() + 1);
            user.updateById();
            // 获取用户头像
            if (StringUtils.isNotEmpty(user.getAvatar())){
                Avatar avatar = avatarService.getById(user.getAvatar());
                user.setPhotoUrl(avatar.getAvatarUrl());
            }

            // 过滤密码
            user.setPassWord("");
            // 生成token
            String token = StringUtils.getUUID();
            //将从用户的数据缓存到redis中
            redisUtil.setEx("USER_TOKEN:" + token, JsonUtils.objectToJson(user), 7, TimeUnit.DAYS);
            return token;
        } else {
            return "0";
        }
    }

    /**
     * 注册
     *
     * @param userVO
     * @return
     */
    @Override
    public String register(UserVO userVO) {
        HttpServletRequest request = RequestHolder.getRequest();
        String ip = IpUtils.getIpAddr(request);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq("user_name", userVO.getUserName()).or().eq("email", userVO.getEmail()));
        queryWrapper.eq("status", EStatus.ENABLE);
        queryWrapper.last("LIMIT 1");
        User user = baseMapper.selectOne(queryWrapper);
        if (user != null) {
            return "用户已存在";
        }
        //获取验证码
        String code = redisUtil.get("REGISTER_CODE:" + userVO.getEmail());
        if (StringUtils.isEmpty(code)){
            return "未获取验证码或者验证码已过期";
        }
        String md5Code = MD5Utils.string2MD5(userVO.getCode());
        if (!code.equals(md5Code)){
            return "验证码不正确";
        }
        user = new User();
        user.setUserName(userVO.getUserName());
        user.setNickName(userVO.getNickName());
        user.setPassWord(MD5Utils.string2MD5(userVO.getPassWord()));
        user.setEmail(userVO.getEmail());
        // 设置账号来源，蘑菇博客
        user.setSource("yixueblog");
        user.setLastLoginIp(ip);

        user.setStatus(EStatus.ENABLE);
        user.insert();


        return "注册成功";
    }

    /**
     * 编辑个人信息
     *
     * @param userVO
     * @return
     */
    @Override
    public boolean editUser(HttpServletRequest request, UserVO userVO) {
        String userUid = request.getAttribute("userUid").toString();
        String token = request.getAttribute("token").toString();
        User user = baseMapper.selectById(userUid);
        if (user == null) {
            return  false;
        }
        user.setNickName(userVO.getNickName());
        user.setAvatar(userVO.getAvatar());
        user.setBirthday(userVO.getBirthday());
        if (StringUtils.isNotEmpty(userVO.getSummary())) {
            user.setSummary(userVO.getSummary());
        } else {
            user.setSummary("这家伙很懒，什么都没有留下");
        }
        user.setGender(userVO.getGender());
        user.setQqNumber(userVO.getQqNumber());
        user.setOccupation(userVO.getOccupation());
        user.setEmail(userVO.getEmail());
        user.updateById();
        user.setPassWord("");
        user.setPhotoUrl(userVO.getPhotoUrl());


        // 修改成功后，更新Redis中的用户信息
        redisUtil.set("USER_TOKEN:" + token, JsonUtils.objectToJson(user), 7,TimeUnit.DAYS);

        return true;
    }

    /**
     * 发送邮件
     * @param email
     * @return
     */
    @Override
    public boolean getCode(String email) {
        //创建邮件正文
        Context context = new Context();
        //验证码
        String verifyCode=StringUtils.generateValidateCode(6).toString();
        context.setVariable("verifyCode", Arrays.asList(verifyCode.split("")));
        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process("registerTemplate", context);
        MimeMessage message=mailSender.createMimeMessage();
        try {
            //true表示需要创建一个multipart message
            MimeMessageHelper helper=new MimeMessageHelper(message,true);
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("易学博客注册验证码");
            helper.setText(emailContent,true);
            mailSender.send(message);
        }catch (MessagingException e) {
            return false;
        }
        //把验证码存储到redis 时间5分钟        key   value(MD5加密)                         time      单位
        redisUtil.set("REGISTER_CODE:"+email,MD5Utils.string2MD5(verifyCode),5,TimeUnit.MINUTES);
        return true;
    }

    /**
     * 获取用户列表
     * @param userVO
     * @return
     */
    @Override
    public IPage<User> getPageList(UserVO userVO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 查询用户名
        if (StringUtils.isNotEmpty(userVO.getKeyword()) && !StringUtils.isEmpty(userVO.getKeyword().trim())) {
            queryWrapper.like("user_name", userVO.getKeyword().trim()).or().like("nick_name", userVO.getKeyword().trim());
        }
        if (StringUtils.isNotEmpty(userVO.getOrderByAscColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(userVO.getOrderByAscColumn())).toString();
            queryWrapper.orderByAsc(column);
        } else if (StringUtils.isNotEmpty(userVO.getOrderByDescColumn())) {
            // 将驼峰转换成下划线
            String column = StringUtils.underLine(new StringBuffer(userVO.getOrderByDescColumn())).toString();
            queryWrapper.orderByDesc(column);
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        queryWrapper.select(User.class, i -> !i.getProperty().equals("pass_word"));
        Page<User> page = new Page<>();
        page.setCurrent(userVO.getCurrentPage());
        page.setSize(userVO.getPageSize());
        queryWrapper.ne("status", EStatus.DISABLED);
        IPage<User> pageList = userService.page(page, queryWrapper);

        List<User> list = pageList.getRecords();

        List<String> avatarUidList = new ArrayList<>();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                avatarUidList.add(item.getAvatar());
            }
        });

        Map<String, String> avatarMap = new HashMap<>();

        List<Avatar> avatarList = null;
        if (avatarUidList.size() != 0) {
            avatarList = avatarService.listByIds(avatarUidList);
        }

        avatarList.forEach(item -> {
            avatarMap.put(item.getUid(), item.getAvatarUrl());
        });

        for (User item : list) {
            //获取图片
            if (StringUtils.isNotEmpty(item.getAvatar())) {
                item.setPhotoUrl(avatarMap.get(item.getAvatar()));
            }
        }
        pageList.setRecords(list);
        return pageList;
    }

    /**
     * 新增用户
     * @param userVO
     */
    @Override
    public String addUser(UserVO userVO) {
        User user = new User();
        // 字段拷贝【将userVO中的内容拷贝至user】
        BeanUtils.copyProperties(userVO, user, "status");
        // 默认密码，建议存储到配置中
        String defaultPassword = "123456";
        user.setPassWord(MD5Utils.string2MD5(defaultPassword));
        user.insert();
        return "添加成功";
    }

    /**
     * 编辑用户
     * @param userVO
     */
    @Override
    public String editUserAdmin(UserVO userVO) {
        User user = userService.getById(userVO.getUid());
        user.setUserName(userVO.getUserName());
        user.setEmail(userVO.getEmail());
        user.setOccupation(userVO.getOccupation());
        user.setGender(userVO.getGender());
        user.setQqNumber(userVO.getQqNumber());
        user.setSummary(userVO.getSummary());
        user.setBirthday(userVO.getBirthday());
        user.setAvatar(userVO.getAvatar());
        user.setNickName(userVO.getNickName());
        user.setUpdateTime(new Date());
        user.updateById();
        return "更新成功";
    }

    /**
     * 删除用户
     * @param userVO
     */
    @Override
    public String deleteUser(UserVO userVO) {
        User user = userService.getById(userVO.getUid());
        user.setStatus(EStatus.DISABLED);
        user.setUpdateTime(new Date());
        user.updateById();
        return "删除成功";
    }

    /**
     * 重置用户密码
     * @param userVO
     * @return
     */
    @Override
    public String resetUserPassword(UserVO userVO) {
        // 默认密码，建议存储到配置上
        String defaultPassword = "123456";
        User user = userService.getById(userVO.getUid());
        user.setPassWord(MD5Utils.string2MD5(defaultPassword));
        user.setUpdateTime(new Date());
        user.updateById();
        return "重置成功";
    }
}
