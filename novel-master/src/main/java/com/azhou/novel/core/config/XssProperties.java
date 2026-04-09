package com.azhou.novel.core.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Xss 过滤配置属性
 *
 * @author azhou
 * @date 2026/03/10
 */
@ConfigurationProperties(prefix = "novel.xss")
public record XssProperties(Boolean enabled, List<String> excludes) {

}
