package com.azhou.novel.service;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.req.FrontAiChatReqDto;
import com.azhou.novel.dto.resp.FrontAiChatRespDto;

/**
 * 前台 AI 服务接口
 *
 * @author azhou
 */
public interface AiService {

    /**
     * AI 找书对话
     *
     * @param dto 请求参数
     * @return 回复内容和推荐书籍
     */
    RestResp<FrontAiChatRespDto> chat(FrontAiChatReqDto dto);
}
