package com.azhou.novel.core.config;

import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.constant.SystemConfigConsts;
import com.azhou.novel.core.interceptor.AuthInterceptor;
import com.azhou.novel.core.interceptor.FileInterceptor;
import com.azhou.novel.core.interceptor.FlowLimitInterceptor;
import com.azhou.novel.core.interceptor.TokenParseInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web Mvc 相关配置不要加 @EnableWebMvc 注解，否则会导致 jackson 的全局配置失效。因为 @EnableWebMvc 注解会导致
 * WebMvcAutoConfiguration 自动配置失效
 *
 * @author xiongxiaoyang
 * @date 2022/5/18
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FlowLimitInterceptor flowLimitInterceptor;

    private final AuthInterceptor authInterceptor;

    private final FileInterceptor fileInterceptor;

    private final TokenParseInterceptor tokenParseInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 流量限制拦截器
        registry.addInterceptor(flowLimitInterceptor)
                .addPathPatterns("/**")
                .order(0);

        // 文件访问拦截
        registry.addInterceptor(fileInterceptor)
                .addPathPatterns(SystemConfigConsts.IMAGE_UPLOAD_DIRECTORY + "**")
                .order(1);

        // 权限认证拦截
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/**",
                        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/**",
                        ApiRouterConsts.API_ADMIN_URL_PREFIX + "/**")
                .excludePathPatterns(ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/register",
                        ApiRouterConsts.API_FRONT_USER_URL_PREFIX + "/login",
                        ApiRouterConsts.API_ADMIN_URL_PREFIX + "/login")
                .order(2);

        // Token 解析拦截器
        registry.addInterceptor(tokenParseInterceptor)
                .addPathPatterns(
                        ApiRouterConsts.API_FRONT_BOOK_URL_PREFIX + "/content/*",
                        ApiRouterConsts.API_FRONT_BOOK_URL_PREFIX + "/tag_cloud")
                .order(3);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //小说封面映射和小说默认封面映射
        registry.addResourceHandler("/localPic/**")
                .addResourceLocations("file:F:\\CodeHome\\JavaHome\\JavaProject\\novel\\test\\novel-master\\upload\\localPic\\");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:F:\\CodeHome\\JavaHome\\JavaProject\\novel\\test\\novel-master\\src\\main\\resources\\static\\");
    }
}
