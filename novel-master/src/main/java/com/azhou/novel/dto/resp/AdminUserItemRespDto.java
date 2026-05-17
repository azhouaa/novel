package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 管理端用户列表项响应 DTO。
 *
 * @author azhou
 * @date 2026/05/17
 */
@Data
@Builder
public class AdminUserItemRespDto {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "账号状态;0-正常 1-封禁")
    private Integer userStatus;

    @Schema(description = "是否作家;1-是 0-否")
    private Integer isAuthor;

    @Schema(description = "作家状态;0-正常 1-封禁 2-待审核")
    private Integer authorStatus;

    @Schema(description = "是否可上传TXT小说;1-是 0-否")
    private Integer canUploadNovel;

    @Schema(description = "是否管理员;1-是 0-否")
    private Integer isAdmin;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
