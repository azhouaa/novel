package com.azhou.novel.service.impl;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.resp.HomeBookRespDto;
import com.azhou.novel.dto.resp.HomeFriendLinkRespDto;
import com.azhou.novel.manager.cache.FriendLinkCacheManager;
import com.azhou.novel.manager.cache.HomeBookCacheManager;
import com.azhou.novel.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页模块 服务实现类
 *
 * @author azhou
 * @date 2026/03/10
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBookCacheManager homeBookCacheManager;

    private final FriendLinkCacheManager friendLinkCacheManager;

    @Override
    public RestResp<List<HomeBookRespDto>> listHomeBooks() {
        return RestResp.ok(homeBookCacheManager.listHomeBooks());
    }

    @Override
    public RestResp<List<HomeFriendLinkRespDto>> listHomeFriendLinks() {
        return RestResp.ok(friendLinkCacheManager.listFriendLinks());
    }
}
