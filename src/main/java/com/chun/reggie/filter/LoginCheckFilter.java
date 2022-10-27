package com.chun.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.chun.reggie.common.BaseContext;
import com.chun.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //path matching
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        log.info("Intercept Request {}",request.getRequestURI() );

        //1. get this request uri
        String requestURI =  request.getRequestURI();

        //2. check pass or intercept
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/user/loginout"

        };

        //3. check login status
        boolean check = check(urls, requestURI);
        if(check){
            log.info("This request {} not need do", requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4. login success then pass
        if(request.getSession().getAttribute("employee") != null){
            log.info("Employee already login!!!");

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            //check current thread id
//            long id = Thread.currentThread().getId();
//            log.info("Current thread id is {}", id);


            filterChain.doFilter(request,response);
            return;
        }



        //Mobile end-point login check
        if(request.getSession().getAttribute("user") != null){
            log.info("User already login, UserId is {}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        /*
        * 5.otherwise return not login status
        * using IO to response the data of client
         * */
        log.info("Not login!!!");
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }


    /*
    * @param requestURI
    *@param urls
    * @return
    * */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }

        }
        return false;
    }
}
 