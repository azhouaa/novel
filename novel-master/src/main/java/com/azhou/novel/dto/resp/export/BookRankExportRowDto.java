package com.azhou.novel.dto.resp.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 排行榜导出行 DTO。
 *
 * <p>该 DTO 仅用于 Excel 导出，不参与接口 JSON 返回。</p>
 *
 * @author azhou
 * @date 2026/4/9
 */
@Data
@Builder
public class BookRankExportRowDto {

    /**
     * 排名（从 1 开始）。
     */
    @ExcelProperty("排名")
    private Integer rankNo;

    /**
     * 分类名称。
     */
    @ExcelProperty("分类")
    private String categoryName;

    /**
     * 书名。
     */
    @ExcelProperty("书名")
    private String bookName;

    /**
     * 作者名。
     */
    @ExcelProperty("作者")
    private String authorName;

    /**
     * 最新章节名称。
     */
    @ExcelProperty("最新章节")
    private String lastChapterName;

    /**
     * 字数。
     */
    @ExcelProperty("字数")
    private Integer wordCount;
}

