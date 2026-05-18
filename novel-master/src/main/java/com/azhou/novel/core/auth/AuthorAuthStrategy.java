package com.azhou.novel.core.auth;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.exception.BusinessException;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.core.util.JwtUtils;
import com.azhou.novel.dao.entity.AuthorInfo;
import com.azhou.novel.dao.mapper.AuthorInfoMapper;
import com.azhou.novel.dto.AuthorInfoDto;
import com.azhou.novel.manager.cache.AuthorInfoCacheManager;
import com.azhou.novel.manager.cache.UserInfoCacheManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    private final AuthorInfoMapper authorInfoMapper;

    /**
     * 不需要作家权限认证的 URI。
     */
    private static final List<String> EXCLUDE_URI = List.of(
        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/register",
        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/status",
        // 上传权限查询接口只需要登录态，不强制要求作家身份，避免管理员账号误报未授权。
        ApiRouterConsts.API_AUTHOR_URL_PREFIX + "/book/upload/permission"
    );

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        // 统一账号认证。
        Long userId = authSSO(jwtUtils, userInfoCacheManager, token);
        if (EXCLUDE_URI.contains(requestUri)) {
            return;
        }

        // 作家权限认证：优先走缓存，缓存未刷新时回源数据库兜底，避免审批通过后短时间误判未授权。
        AuthorInfoDto authorInfo = authorInfoCacheManager.getAuthor(userId);
        Long authorId = null;
        if (authorInfo != null && Objects.equals(authorInfo.getStatus(), 0)) {
            authorId = authorInfo.getId();
        } else {
            QueryWrapper<AuthorInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(DatabaseConsts.AuthorInfoTable.COLUMN_USER_ID, userId)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
            AuthorInfo authorInfoEntity = authorInfoMapper.selectOne(queryWrapper);
            if (authorInfoEntity != null && Objects.equals(authorInfoEntity.getStatus(), 0)) {
                authorId = authorInfoEntity.getId();
            }
        }

        if (authorId == null) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }

        // 设置作者ID到当前线程。
        UserHolder.setAuthorId(authorId);
    }
}
