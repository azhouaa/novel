package com.azhou.novel.controller.admin;

import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.ApiRouterConsts;
import com.azhou.novel.core.constant.SystemConfigConsts;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;
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
}
