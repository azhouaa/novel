package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前台 AI 找书对话响应 DTO
 *
 * @author azhou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontAiChatRespDto {

    @Schema(description = "AI 回复内容")
    private String answer;

    @Schema(description = "推荐书籍列表")
    private List<BookItem> books;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookItem {

        @Schema(description = "书籍 ID", example = "1")
        private Long id;

        @Schema(description = "书名", example = "斗破苍穹")
        private String bookName;

        @Schema(description = "作者", example = "天蚕土豆")
        private String authorName;

        @Schema(description = "分类", example = "玄幻")
        private String categoryName;

        @Schema(description = "详情页链接（前端 hash 路由）", example = "/#/book/1")
        private String detailUrl;
    }
}
