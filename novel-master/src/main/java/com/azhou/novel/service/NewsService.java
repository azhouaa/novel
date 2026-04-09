package com.azhou.novel.service;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.resp.NewsInfoRespDto;

import java.util.List;

/**
 * 新闻模块 服务类
 *
 * @author azhou
 * @date 2026/03/10
 */
public interface NewsService {

    /**
     * 最新新闻列表查询
     *
     * @return 新闻列表
     */
    RestResp<List<NewsInfoRespDto>> listLatestNews();

    /**
     * 新闻信息查询
     *
     * @param id 新闻ID
     * @return 新闻信息
     */
    RestResp<NewsInfoRespDto> getNews(Long id);
}
