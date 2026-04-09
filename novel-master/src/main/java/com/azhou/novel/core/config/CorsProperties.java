package com.azhou.novel.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 跨域配置属性
 *
 * @author azhou
 * @date 2026/03/10
 */
@ConfigurationProperties(prefix = "novel.cors")
public record CorsProperties(List<String> allowOrigins) {

}
