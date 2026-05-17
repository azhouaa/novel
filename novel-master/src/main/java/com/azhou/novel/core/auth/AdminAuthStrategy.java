package com.azhou.novel.core.auth;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.exception.BusinessException;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.util.JwtUtils;
import com.azhou.novel.dto.UserInfoDto;
import com.azhou.novel.manager.cache.UserInfoCacheManager;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 平台后台管理系统认证授权策略。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Component
@RequiredArgsConstructor
public class AdminAuthStrategy implements AuthStrategy {

    private final JwtUtils jwtUtils;

    private final UserInfoCacheManager userInfoCacheManager;

    private static final String ADMIN_LOGIN_URI = ApiRouterConsts.API_ADMIN_URL_PREFIX + "/login";

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        if (ADMIN_LOGIN_URI.equals(requestUri)) {
            return;
        }

        Long userId = authSSO(jwtUtils, userInfoCacheManager, token);
        UserInfoDto userInfo = userInfoCacheManager.getUser(userId);
        if (userInfo == null || !Objects.equals(userInfo.getStatus(), 0)
            || !Objects.equals(userInfo.getIsAdmin(), 1)) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }
    }
}
