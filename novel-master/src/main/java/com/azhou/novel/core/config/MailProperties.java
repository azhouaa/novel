package com.azhou.novel.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mail 配置属性
 *
 * @author azhou
 * @date 2026/03/10
 */
@ConfigurationProperties(prefix = "spring.mail")
public record MailProperties(String nickname, String username) {

}
