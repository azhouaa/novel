package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 作家端书评管理列表项 DTO。
 *
 * @author azhou
 * @date 2026/05/18
 */
@Data
@Builder
public class AuthorCommentItemRespDto {

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "书籍ID")
    private Long bookId;

    @Schema(description = "书名")
    private String bookName;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户名")
    private String username;

    @Schema(description = "评论内容")
    private String commentContent;

    @Schema(description = "评论状态;0-待审 1-通过 2-驳回")
    private Integer auditStatus;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
