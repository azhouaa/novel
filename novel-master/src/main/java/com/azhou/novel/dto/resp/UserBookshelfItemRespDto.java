package com.azhou.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 书架条目响应 DTO
 *
 * @author azhou
 * @date 2026/4/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookshelfItemRespDto {

    /**
     * 小说ID
     */
    @Schema(description = "小说ID")
    private Long bookId;

    /**
     * 小说名称
     */
    @Schema(description = "小说名称")
    private String bookName;

    /**
     * 封面地址
     */
    @Schema(description = "封面地址")
    private String picUrl;

    /**
     * 作者名称
     */
    @Schema(description = "作者名称")
    private String authorName;

    /**
     * 续读章节ID（点击书籍时用于跳转）
     */
    @Schema(description = "续读章节ID")
    private Long continueChapterId;

    /**
     * 当前阅读章节ID
     */
    @Schema(description = "当前阅读章节ID")
    private Long lastReadChapterId;

    /**
     * 当前阅读章节名
     */
    @Schema(description = "当前阅读章节名")
    private String lastReadChapterName;

    /**
     * 最新章节名
     */
    @Schema(description = "最新章节名")
    private String lastChapterName;

    /**
     * 加书架/更新进度时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;
}
