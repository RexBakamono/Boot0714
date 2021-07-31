package com.rex.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

//@PropertySource("classpath:application.yml")定位配置文件，默认application.yml
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.open}")
    private boolean isOpenJwt;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) {
        // 从 http 请求头中取出 token
        String token = httpServletRequest.getHeader("token");
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        // 存在@JwtToken注解的方法跳过token验证
        if (!method.isAnnotationPresent(JwtToken.class) && isOpenJwt) {
            // 执行认证
            if (token == null) {
                throw new RuntimeException("无token，请重新登录");
            }
            // 获取 token 中的 userId
            String userId = JwtUtil.getUserId(token);
            System.out.println("用户id:" + userId);

            // 验证 token
            JwtUtil.checkSign(token);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

    }
}