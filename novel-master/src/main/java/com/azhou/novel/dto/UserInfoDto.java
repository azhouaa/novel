package com.azhou.novel.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 DTO。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Data
@Builder
public class UserInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private Integer status;

    private Integer canUploadNovel;

    private Integer isAdmin;
}
