package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 检查用户是否已经登录。
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();

        log.info("拦截到请求: {}", requestURI);

        // 定义不需要处理的请求路径。
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/frontend/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 检查请求是否在不需要请求的路径内。
        if (check(urls, requestURI)) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否已经登录
//        Object id = request.getSession().getAttribute("employee");
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录, 用户id为: {}", request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录。");

        // 判断移动端用户是否登录。
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录, 用户id为: {}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");

            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录。");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }


    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
