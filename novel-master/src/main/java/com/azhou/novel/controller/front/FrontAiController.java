package com.azhou.novel.controller.front;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.dto.req.FrontAiChatReqDto;
import com.azhou.novel.dto.resp.FrontAiChatRespDto;
import com.azhou.novel.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台门户-AI 模块 API 控制器
 *
 * @author azhou
 */
@Tag(name = "FrontAiController", description = "前台门户-AI 模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_AI_URL_PREFIX)
@RequiredArgsConstructor
public class FrontAiController {

    private final AiService aiService;

    /**
     * AI 找书对话接口
     */
    @Operation(summary = "AI 找书对话接口")
    @PostMapping("chat")
    public RestResp<FrontAiChatRespDto> chat(@Valid @RequestBody FrontAiChatReqDto dto) {
        return aiService.chat(dto);
    }
}
