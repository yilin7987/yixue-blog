package com.yilin.yixueblog.web.filter;


import com.yilin.yixueblog.utils.JsonUtils;
import com.yilin.yixueblog.utils.RedisUtil;
import com.yilin.yixueblog.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 拦截器
 */
@Component
@Slf4j
public class AuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtil redisUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //得到请求头信息authorization信息
        String accessToken = request.getHeader("Authorization");

        if (accessToken != null) {
            //从Redis中获取内容
            String userInfo = redisUtil.get("USER_TOKEN:" + accessToken);
            if (!StringUtils.isEmpty(userInfo)) {
                Map<String, Object> map = JsonUtils.jsonToMap(userInfo);
                //把userUid存储到 request中
                request.setAttribute("token", accessToken);
                request.setAttribute("userUid", map.get("uid"));
                request.setAttribute("userName", map.get("nickName"));
//                log.info("解析出来的用户:{}", map.get(SysConf.NICK_NAME));
            }
        }
        chain.doFilter(request, response);
    }
}


