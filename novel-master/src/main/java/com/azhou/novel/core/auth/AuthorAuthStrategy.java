package com.azhou.novel.core.auth;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.exception.BusinessException;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.util.JwtUtils;
import com.azhou.novel.dto.AuthorInfoDto;
import com.azhou.novel.manager.cache.AuthorInfoCacheManager;
import com.azhou.novel.manager.cache.UserInfoCacheManager;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 作家后台管理系统认证授权策略。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Component
@RequiredArgsConstructor
public class AuthorAuthStrategy implements AuthStrategy {

    private final JwtUtils jwtUtils;

    private final UserInfoCacheManager userInfoCacheManager;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    /**
     * 不需要作者权限认证的 URI。
     */
    private static final List<String> EXCLUDE_URI = List.of(
        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/register",
        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/status"
    );

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        // 统一账号认证。
        Long userId = authSSO(jwtUtils, userInfoCacheManager, token);
        if (EXCLUDE_URI.contains(requestUri)) {
            return;
        }

        // 作者权限认证：必须存在作者记录且状态为正常(0)。
        AuthorInfoDto authorInfo = authorInfoCacheManager.getAuthor(userId);
        if (authorInfo == null || !Objects.equals(authorInfo.getStatus(), 0)) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }

        // 设置作者 ID 到当前线程。
        UserHolder.setAuthorId(authorInfo.getId());
    }
}
