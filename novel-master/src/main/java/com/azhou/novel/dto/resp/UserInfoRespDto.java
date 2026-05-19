package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息响应 DTO。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Data
@Builder
public class UserInfoRespDto {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "用户性别")
    private Integer userSex;

    @Schema(description = "用户偏好标签，逗号分隔")
    private String preferTags;

    @Schema(description = "是否作家;1-是 0-否")
    private Integer isAuthor;

    @Schema(description = "作家状态;0-正常 1-封禁 2-待审核")
    private Integer authorStatus;

    @Schema(description = "是否可上传TXT小说;1-是 0-否")
    private Integer canUploadNovel;

    @Schema(description = "是否管理员;1-是 0-否")
    private Integer isAdmin;

    @Schema(description = "作家笔名")
    private String penName;
}