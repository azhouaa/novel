package com.azhou.novel.controller.admin;

import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.constant.SystemConfigConsts;
import com.azhou.novel.dto.resp.AdminAuditBookItemRespDto;
import com.azhou.novel.dto.resp.AdminAuditChapterItemRespDto;
import com.azhou.novel.dto.resp.AdminCommentItemRespDto;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;
import com.azhou.novel.dto.resp.ChapterContentRespDto;
import com.azhou.novel.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台后台管理接口。
 *
 * @author azhou
 * @date 2026/05/17
 */
@Tag(name = "AdminController", description = "平台后台管理接口")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_ADMIN_URL_PREFIX)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 分页查询用户列表。
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("users")
    public RestResp<PageRespDto<AdminUserItemRespDto>> listUsers(@ParameterObject PageReqDto dto) {
        return adminService.listUsers(dto);
    }

    /**
     * 同意作家申请。
     */
    @Operation(summary = "同意作家申请")
    @PostMapping("author/approve/{userId}")
    public RestResp<Void> approveAuthor(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return adminService.approveAuthor(userId);
    }

    /**
     * 撤销作家权限。
     */
    @Operation(summary = "撤销作家权限")
    @PostMapping("author/revoke/{userId}")
    public RestResp<Void> revokeAuthor(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return adminService.revokeAuthor(userId);
    }

    /**
     * 封禁账号。
     */
    @Operation(summary = "封禁账号")
    @PostMapping("user/ban/{userId}")
    public RestResp<Void> banUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return adminService.banUser(userId);
    }

    /**
     * 解除封禁。
     */
    @Operation(summary = "解除封禁")
    @PostMapping("user/unban/{userId}")
    public RestResp<Void> unbanUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return adminService.unbanUser(userId);
    }

    /**
     * 删除小说。
     */
    @Operation(summary = "删除小说")
    @PostMapping("book/delete/{bookId}")
    public RestResp<Void> deleteBook(@Parameter(description = "小说ID") @PathVariable Long bookId) {
        return adminService.deleteBook(bookId);
    }

    /**
     * 按书名删除小说（演示版）。
     */
    @Operation(summary = "按书名删除小说")
    @PostMapping("book/delete_by_name")
    public RestResp<Void> deleteBookByName(@Parameter(description = "小说名") @RequestParam("bookName") String bookName) {
        return adminService.deleteBookByName(bookName);
    }

    /**
     * 分页查询待审核书籍。
     */
    @Operation(summary = "分页查询待审核书籍")
    @GetMapping("audit/books/pending")
    public RestResp<PageRespDto<AdminAuditBookItemRespDto>> listPendingBooks(@ParameterObject PageReqDto dto) {
        return adminService.listPendingBooks(dto);
    }

    /**
     * 分页查询待审核章节。
     */
    @Operation(summary = "分页查询待审核章节")
    @GetMapping("audit/chapters/pending")
    public RestResp<PageRespDto<AdminAuditChapterItemRespDto>> listPendingChapters(@ParameterObject PageReqDto dto) {
        return adminService.listPendingChapters(dto);
    }

    /**
     * 审核书籍。
     */
    @Operation(summary = "审核书籍")
    @PostMapping("audit/book/{bookId}")
    public RestResp<Void> auditBook(
        @Parameter(description = "小说ID") @PathVariable Long bookId,
        @Parameter(description = "是否通过") @RequestParam("pass") Boolean pass) {
        return adminService.auditBook(bookId, Boolean.TRUE.equals(pass));
    }

    /**
     * 审核章节。
     */
    @Operation(summary = "审核章节")
    @PostMapping("audit/chapter/{chapterId}")
    public RestResp<Void> auditChapter(
        @Parameter(description = "章节ID") @PathVariable Long chapterId,
        @Parameter(description = "是否通过") @RequestParam("pass") Boolean pass) {
        return adminService.auditChapter(chapterId, Boolean.TRUE.equals(pass));
    }

    /**
     * 管理员查询章节详情（审核预览）。
     */
    @Operation(summary = "管理员查询章节详情")
    @GetMapping("audit/chapter/{chapterId}")
    public RestResp<ChapterContentRespDto> getChapterDetail(
        @Parameter(description = "章节ID") @PathVariable Long chapterId) {
        return adminService.getChapterDetail(chapterId);
    }

    /**
     * 管理员分页查看全部评论。
     */
    @Operation(summary = "管理员分页查看全部评论")
    @GetMapping("comments")
    public RestResp<PageRespDto<AdminCommentItemRespDto>> listAllComments(@ParameterObject PageReqDto dto) {
        return adminService.listAllComments(dto);
    }

    /**
     * 管理员删除评论。
     */
    @Operation(summary = "管理员删除评论")
    @PostMapping("comment/delete/{commentId}")
    public RestResp<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable Long commentId) {
        return adminService.deleteComment(commentId);
    }
}
