package com.sj.filter;

import com.alibaba.fastjson.JSONObject;
import com.sj.domain.ResponseResult;
import com.sj.domain.entity.LoginUser;
import com.sj.utils.JwtUtils;
import com.sj.utils.RedisCache;
import com.sj.utils.WebUtils;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static com.sj.enums.AppHttpCodeEnum.NEED_LOGIN;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisCache redisCache;

    /**
     *
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        Claims claims = null;
        try {
            claims = JwtUtils.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseResult errorResult = ResponseResult.errorResult(NEED_LOGIN);
            WebUtils.renderString(response, JSONObject.toJSONString(errorResult));
            return;
        }
        String userId = claims.getSubject();

        LoginUser loginUser = redisCache.getCacheObject("login: " + userId);
        if (Objects.isNull(loginUser)){
            ResponseResult errorResult = ResponseResult.errorResult(NEED_LOGIN);
            WebUtils.renderString(response, JSONObject.toJSONString(errorResult));
            return;
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(request, response);
    }
}