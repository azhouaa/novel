package com.azhou.novel.manager.cache;

import com.azhou.novel.core.constant.CacheConsts;
import com.azhou.novel.dao.entity.UserInfo;
import com.azhou.novel.dao.mapper.UserInfoMapper;
import com.azhou.novel.dto.UserInfoDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 用户信息缓存管理类。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Component
@RequiredArgsConstructor
public class UserInfoCacheManager {

    private final UserInfoMapper userInfoMapper;

    /**
     * 查询用户信息，并放入缓存。
     */
    @Cacheable(cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
        value = CacheConsts.USER_INFO_CACHE_NAME)
    public UserInfoDto getUser(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (Objects.isNull(userInfo)) {
            return null;
        }
        return UserInfoDto.builder()
            .id(userInfo.getId())
            .username(userInfo.getUsername())
            .status(userInfo.getStatus())
            .canUploadNovel(userInfo.getCanUploadNovel())
            .isAdmin(userInfo.getIsAdmin())
            .build();
    }

    /**
     * 清理用户信息缓存，管理员改权限后调用。
     */
    @CacheEvict(cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
        value = CacheConsts.USER_INFO_CACHE_NAME, allEntries = true)
    public void evictUserCache() {
        // 直接清空全部用户缓存，保证权限修改后立即生效。
    }
}
