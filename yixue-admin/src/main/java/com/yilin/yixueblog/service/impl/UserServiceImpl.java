package com.yilin.yixueblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yilin.yixueblog.*;
import com.yilin.yixueblog.entity.Avatar;
import com.yilin.yixueblog.entity.User;
import com.yilin.yixueblog.enums.EStatus;
import com.yilin.yixueblog.mapper.UserMapper;
import com.yilin.yixueblog.service.AvatarService;
import com.yilin.yixueblog.service.UserService;
import com.yilin.yixueblog.vo.UserVO;
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
import java.util.Arrays;
import java.util.Date;
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

    /**
     * 邮件发件人
     */
    @Value("${mail.fromMail.fromAddress}")
    private String fromAddress;

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
//        log.info("获取到的用户: {}", user);
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
}
