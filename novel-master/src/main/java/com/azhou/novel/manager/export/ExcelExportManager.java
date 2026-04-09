package com.azhou.novel.manager.export;

import com.alibaba.excel.EasyExcel;
import com.azhou.novel.dto.resp.BookRankRespDto;
import com.azhou.novel.dto.resp.UserBookshelfItemRespDto;
import com.azhou.novel.dto.resp.export.BookRankExportRowDto;
import com.azhou.novel.dto.resp.export.UserBookshelfExportRowDto;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Excel 导出管理器。
 *
 * <p>统一封装排行榜与书架的 Excel 输出行为，避免在 Controller 中重复处理响应头、
 * 数据映射和 EasyExcel 调用逻辑。</p>
 *
 * @author azhou
 * @date 2026/4/9
 */
@Component
@Slf4j
public class ExcelExportManager {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出排行榜数据。
     *
     * @param response Http 响应
     * @param rankName 榜单中文名（用于文件名和 sheet 名）
     * @param rankBooks 排行榜数据
     */
    public void exportBookRank(HttpServletResponse response, String rankName, List<BookRankRespDto> rankBooks) {
        List<BookRankRespDto> safeRankBooks = Objects.requireNonNullElse(rankBooks, List.of());
        List<BookRankExportRowDto> rows = IntStream.range(0, safeRankBooks.size())
            .mapToObj(i -> BookRankExportRowDto.builder()
                .rankNo(i + 1)
                .categoryName(safeRankBooks.get(i).getCategoryName())
                .bookName(safeRankBooks.get(i).getBookName())
                .authorName(safeRankBooks.get(i).getAuthorName())
                .lastChapterName(safeRankBooks.get(i).getLastChapterName())
                .wordCount(safeRankBooks.get(i).getWordCount())
                .build())
            .toList();
        writeExcel(response, rankName + "导出", rankName, BookRankExportRowDto.class, rows);
    }

    /**
     * 导出书架数据。
     *
     * @param response Http 响应
     * @param shelfItems 书架条目
     */
    public void exportBookshelf(HttpServletResponse response, List<UserBookshelfItemRespDto> shelfItems) {
        List<UserBookshelfItemRespDto> safeShelfItems = Objects.requireNonNullElse(shelfItems, List.of());
        List<UserBookshelfExportRowDto> rows = safeShelfItems.stream()
            .map(item -> UserBookshelfExportRowDto.builder()
                .bookName(item.getBookName())
                .authorName(item.getAuthorName())
                .lastReadChapterName(item.getLastReadChapterName())
                .lastChapterName(item.getLastChapterName())
                .updateTime(item.getUpdateTime() == null ? "" : item.getUpdateTime().format(DATETIME_FORMATTER))
                .build())
            .toList();
        writeExcel(response, "我的书架导出", "我的书架", UserBookshelfExportRowDto.class, rows);
    }

    /**
     * 统一写入 Excel 到响应流。
     *
     * <p>文件名使用 UTF-8 URL 编码，兼容中文下载名；响应类型固定为 xlsx。</p>
     *
     * @param response Http 响应
     * @param fileName 文件名（不带后缀）
     * @param sheetName sheet 名称
     * @param headClass 表头映射类
     * @param rows 导出行数据
     */
    private <T> void writeExcel(HttpServletResponse response, String fileName, String sheetName, Class<T> headClass,
        List<T> rows) {
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), headClass).sheet(sheetName).doWrite(rows);
        } catch (IOException e) {
            // 导出接口需要记录详细日志，便于排查浏览器下载失败和编码问题。
            log.error("Excel 导出失败，fileName:{}", fileName, e);
            throw new IllegalStateException("Excel 导出失败", e);
        }
    }
}
