package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 作者上传记录响应DTO
 *
 * @author azhou
 * @date 2026/04/14
 */
@Data
@Builder
public class AuthorUploadRecordRespDto {

    /**
     * 上传记录ID
     */
    @Schema(description = "上传记录ID")
    private Long id;

    /**
     * 上传用户ID
     */
    @Schema(description = "上传用户ID")
    private Long userId;

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
     * 作者名称
     */
    @Schema(description = "作者名称")
    private String authorName;

    /**
     * TXT文件名
     */
    @Schema(description = "TXT文件名")
    private String txtFileName;

    /**
     * TXT编码
     */
    @Schema(description = "TXT编码")
    private String txtCharset;

    /**
     * 章节总数
     */
    @Schema(description = "章节总数")
    private Integer chapterTotal;

    /**
     * 总字数
     */
    @Schema(description = "总字数")
    private Integer wordTotal;

    /**
     * 处理耗时毫秒
     */
    @Schema(description = "处理耗时毫秒")
    private Long durationMs;

    /**
     * 状态;1-成功 2-失败
     */
    @Schema(description = "状态;1-成功 2-失败")
    private Integer status;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime createTime;
}
