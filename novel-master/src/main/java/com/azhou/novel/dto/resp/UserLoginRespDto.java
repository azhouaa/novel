package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录 响应DTO
 * @author azhou
 * @date 2026/03/10
 */
@Data
@Builder
public class UserLoginRespDto {

    @Schema(description = "用户ID")
    private Long uid;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户token")
    private String token;
}
