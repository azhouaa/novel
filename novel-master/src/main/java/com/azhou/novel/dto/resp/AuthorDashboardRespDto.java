package com.azhou.novel.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 作家专区大屏统计响应DTO。
 *
 * @author azhou
 * @date 2026/05/18
 */
@Data
@Builder
public class AuthorDashboardRespDto {

    /**
     * 当前作家的小说总数。
     */
    private Long totalBooks;

    /**
     * 当前作家的章节总数。
     */
    private Long totalChapters;

    /**
     * 当前作家书评总数。
     */
    private Long totalComments;

    /**
     * 当前作家待审核小说数。
     */
    private Long pendingBooks;

    /**
     * 当前作家待审核章节数。
     */
    private Long pendingChapters;

    /**
     * 当前用户上传记录总数。
     */
    private Long totalUploadRecords;
}
