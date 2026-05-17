package com.azhou.novel.core.auth;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.exception.BusinessException;
import com.azhou.novel.core.constant.SystemConfigConsts;
import com.azhou.novel.core.util.JwtUtils;
import com.azhou.novel.dto.UserInfoDto;
import com.azhou.novel.manager.cache.UserInfoCacheManager;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * 策略模式实现用户认证授权功能。
 *
 * @author azhou
 * @date 2026/03/10
 */
public interface AuthStrategy {

    /**
     * 用户认证授权。
     *
     * @param token 登录 token
     * @param requestUri 请求 URI
     * @throws BusinessException 认证失败则抛出业务异常
     */
    void auth(String token, String requestUri) throws BusinessException;

    /**
     * 前台多系统单点登录统一账号认证授权。
     *
     * @param jwtUtils jwt 工具
     * @param userInfoCacheManager 用户缓存管理对象
     * @param token 登录 token
     * @return 用户ID
     */
    default Long authSSO(JwtUtils jwtUtils, UserInfoCacheManager userInfoCacheManager,
        String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        Long userId = jwtUtils.parseToken(token, SystemConfigConsts.NOVEL_FRONT_KEY);
        if (Objects.isNull(userId)) {
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        UserInfoDto userInfo = userInfoCacheManager.getUser(userId);
        if (Objects.isNull(userInfo)) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        // 账号封禁后禁止访问登录态接口。
        if (Objects.equals(userInfo.getStatus(), 1)) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }
        UserHolder.setUserId(userId);
        return userId;
    }
}
