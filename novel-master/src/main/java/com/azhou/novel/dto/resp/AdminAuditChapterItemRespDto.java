package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 管理端待审核章节列表项 DTO。
 *
 * @author azhou
 * @date 2026/05/18
 */
@Data
@Builder
public class AdminAuditChapterItemRespDto {

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "小说ID")
    private Long bookId;

    @Schema(description = "书名")
    private String bookName;

    @Schema(description = "章节名")
    private String chapterName;

    @Schema(description = "审核状态;0-待审 1-通过 2-驳回")
    private Integer auditStatus;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
