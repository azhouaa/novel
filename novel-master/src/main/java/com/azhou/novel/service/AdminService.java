package com.azhou.novel.service;

import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;

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
}
