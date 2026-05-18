package com.azhou.novel.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 管理后台大屏统计响应DTO。
 *
 * @author azhou
 * @date 2026/05/18
 */
@Data
@Builder
public class AdminDashboardRespDto {

    /**
     * 用户总数。
     */
    private Long totalUsers;

    /**
     * 作家总数。
     */
    private Long totalAuthors;

    /**
     * 小说总数。
     */
    private Long totalBooks;

    /**
     * 章节总数。
     */
    private Long totalChapters;

    /**
     * 评论总数。
     */
    private Long totalComments;

    /**
     * 待审核小说数。
     */
    private Long pendingBooks;

    /**
     * 待审核章节数。
     */
    private Long pendingChapters;

    /**
     * 上传记录总数。
     */
    private Long totalUploadRecords;
}
