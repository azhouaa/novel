package com.azhou.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 响应DTO
 *
 * @author xiongxiaoyang
 * @date 2022/5/22
 */
@Data
@Builder
public class UserInfoRespDto {

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "用户头像")
    private String userPhoto;

    @Schema(description = "用户性别")
    private Integer userSex;

    @Schema(description = "用户喜好标签，逗号分隔")
    private String preferTags;
}
