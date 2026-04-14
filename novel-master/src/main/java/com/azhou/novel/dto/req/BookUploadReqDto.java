package com.azhou.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小说 TXT 上传请求 DTO
 *
 * @author azhou
 * @date 2026/04/14
 */
@Data
public class BookUploadReqDto {

    /**
     * 作品方向：0-男频，1-女频
     */
    @Schema(description = "作品方向：0-男频，1-女频", required = true)
    @NotNull
    private Integer workDirection;

    /**
     * 分类 ID
     */
    @Schema(description = "分类 ID", required = true)
    @NotNull
    private Long categoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", required = true)
    @NotBlank
    private String categoryName;

    /**
     * 小说封面地址
     */
    @Schema(description = "小说封面地址", required = true)
    @NotBlank
    private String picUrl;

    /**
     * 小说名称
     */
    @Schema(description = "小说名称", required = true)
    @NotBlank
    private String bookName;

    /**
     * 小说作者名称
     */
    @Schema(description = "小说作者名称", required = true)
    @NotBlank
    private String authorName;

    /**
     * 小说简介
     */
    @Schema(description = "小说简介")
    private String bookDesc;

    /**
     * 是否收费：1-收费，0-免费
     */
    @Schema(description = "是否收费：1-收费，0-免费", required = true)
    @NotNull
    private Integer isVip;
}
