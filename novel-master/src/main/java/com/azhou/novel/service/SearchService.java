package com.azhou.novel.service;

import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.req.BookSearchReqDto;
import com.azhou.novel.dto.resp.BookInfoRespDto;

/**
 * 搜索 服务类
 *
 * @author azhou
 * @date 2026/03/10
 */
public interface SearchService {

    /**
     * 小说搜索
     *
     * @param condition 搜索条件
     * @return 搜索结果
     */
    RestResp<PageRespDto<BookInfoRespDto>> searchBooks(BookSearchReqDto condition);

}
