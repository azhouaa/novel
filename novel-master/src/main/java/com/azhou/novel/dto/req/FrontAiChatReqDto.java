package com.azhou.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 前台 AI 找书对话请求 DTO
 *
 * @author azhou
 */
@Data
public class FrontAiChatReqDto {

    @Schema(description = "用户当前输入内容", required = true, example = "推荐一些男频玄幻爽文，节奏快")
    @NotBlank(message = "消息内容不能为空！")
    private String message;

    @Schema(description = "对话历史（可选）")
    private List<HistoryItem> history;

    @Data
    public static class HistoryItem {

        @Schema(description = "对话角色（user / assistant）", example = "user")
        private String role;

        @Schema(description = "对话内容", example = "我喜欢悬疑推理")
        private String content;
    }
}
