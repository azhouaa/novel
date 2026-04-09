package com.azhou.novel.dto.resp.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 书架导出行 DTO。
 *
 * <p>导出字段按前端书架展示语义组织，方便运营或用户离线查看。</p>
 *
 * @author azhou
 * @date 2026/4/9
 */
@Data
@Builder
public class UserBookshelfExportRowDto {

    /**
     * 书名。
     */
    @ExcelProperty("书名")
    private String bookName;

    /**
     * 作者。
     */
    @ExcelProperty("作者")
    private String authorName;

    /**
     * 当前阅读章节名称。
     */
    @ExcelProperty("阅读到章节")
    private String lastReadChapterName;

    /**
     * 最新章节名称。
     */
    @ExcelProperty("最新章节")
    private String lastChapterName;

    /**
     * 更新时间（字符串展示，避免 Excel 自动格式化误差）。
     */
    @ExcelProperty("最近更新时间")
    private String updateTime;
}

