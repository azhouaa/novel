package com.azhou.novel.core.auth;

import com.azhou.novel.core.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 平台后台管理系统 认证授权策略
 *
 * @author azhou
 * @date 2026/03/10
 */
@Component
@RequiredArgsConstructor
public class AdminAuthStrategy implements AuthStrategy {

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        // TODO 平台后台 token 校验
    }
    
}