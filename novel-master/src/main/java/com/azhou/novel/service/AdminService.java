package com.azhou.novel.service;

import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.resp.AdminAuditBookItemRespDto;
import com.azhou.novel.dto.resp.AdminAuditChapterItemRespDto;
import com.azhou.novel.dto.resp.AdminDashboardRespDto;
import com.azhou.novel.dto.resp.AdminCommentItemRespDto;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;
import com.azhou.novel.dto.resp.ChapterContentRespDto;

/**
 * 管理后台服务接口。
 *
 * @author azhou
 * @date 2026/05/17
 */
public interface AdminService {

    /**
     * 分页查询平台用户及权限信息。
     *
     * @param dto 分页参数
     * @return 用户分页数据
     */
    RestResp<PageRespDto<AdminUserItemRespDto>> listUsers(PageReqDto dto);

    /**
     * 管理后台大屏统计。
     *
     * @return 统计数据
     */
    RestResp<AdminDashboardRespDto> getDashboard();

    /**
     * 同意作家申请，并自动开通上传权限。
     *
     * @param userId 用户ID
     * @return void
     */
    RestResp<Void> approveAuthor(Long userId);

    /**
     * 封禁账号。
     *
     * @param userId 用户ID
     * @return void
     */
    RestResp<Void> banUser(Long userId);

    /**
     * 解除账号封禁。
     *
     * @param userId 用户ID
     * @return void
     */
    RestResp<Void> unbanUser(Long userId);

    /**
     * 撤销作家权限，并自动关闭上传权限。
     *
     * @param userId 用户ID
     * @return void
     */
    RestResp<Void> revokeAuthor(Long userId);

    /**
     * 管理员删除小说（包含章节与正文）。
     *
     * @param bookId 小说ID
     * @return void
     */
    RestResp<Void> deleteBook(Long bookId);

    /**
     * 管理员按书名删除小说（演示版，命中同名第一本）。
     *
     * @param bookName 小说名
     * @return void
     */
    RestResp<Void> deleteBookByName(String bookName);

    /**
     * 分页查询待审核书籍列表。
     *
     * @param dto 分页参数
     * @return 待审核书籍分页数据
     */
    RestResp<PageRespDto<AdminAuditBookItemRespDto>> listPendingBooks(PageReqDto dto);

    /**
     * 分页查询待审核章节列表。
     *
     * @param dto 分页参数
     * @return 待审核章节分页数据
     */
    RestResp<PageRespDto<AdminAuditChapterItemRespDto>> listPendingChapters(PageReqDto dto);

    /**
     * 审核书籍。
     *
     * @param bookId 书籍ID
     * @param pass true-通过 false-驳回
     * @return void
     */
    RestResp<Void> auditBook(Long bookId, boolean pass);

    /**
     * 审核章节。
     *
     * @param chapterId 章节ID
     * @param pass true-通过 false-驳回
     * @return void
     */
    RestResp<Void> auditChapter(Long chapterId, boolean pass);

    /**
     * 管理员查询章节详情（用于审核预览）。
     *
     * @param chapterId 章节ID
     * @return 章节内容详情
     */
    RestResp<ChapterContentRespDto> getChapterDetail(Long chapterId);

    /**
     * 管理员分页查看全部书评。
     *
     * @param dto 分页参数
     * @return 书评分页数据
     */
    RestResp<PageRespDto<AdminCommentItemRespDto>> listAllComments(PageReqDto dto);

    /**
     * 管理员删除书评。
     *
     * @param commentId 评论ID
     * @return void
     */
    RestResp<Void> deleteComment(Long commentId);
}
