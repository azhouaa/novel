package com.azhou.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.dao.entity.BookInfo;
import com.azhou.novel.dao.mapper.BookInfoMapper;
import com.azhou.novel.dto.req.FrontAiChatReqDto;
import com.azhou.novel.dto.resp.FrontAiChatRespDto;
import com.azhou.novel.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 前台 AI 服务实现
 *
 * @author azhou
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private static final int MAX_KEYWORD_COUNT = 6;

    private static final int MAX_RESULT_COUNT = 5;

    private static final String COLUMN_AUTHOR_NAME = "author_name";

    private static final String COLUMN_CATEGORY_NAME = "category_name";

    private static final String COLUMN_BOOK_DESC = "book_desc";

    private final BookInfoMapper bookInfoMapper;

    private final ChatClient chatClient;

    @Override
    public RestResp<FrontAiChatRespDto> chat(FrontAiChatReqDto dto) {
        List<String> keywords = buildKeywords(dto.getMessage());
        List<BookInfo> books = searchBooksFromDatabase(keywords);
        List<FrontAiChatRespDto.BookItem> bookItems = books.stream()
            .limit(MAX_RESULT_COUNT)
            .map(this::convertBookItem)
            .toList();

        String answer = buildAnswer(dto, bookItems);

        FrontAiChatRespDto respDto = FrontAiChatRespDto.builder()
            .answer(answer)
            .books(bookItems)
            .build();
        return RestResp.ok(respDto);
    }

    private List<String> buildKeywords(String message) {
        if (!StringUtils.hasText(message)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> keywordSet = new LinkedHashSet<>();
        String normalized = message.trim();
        keywordSet.add(normalized);

        Matcher matcher = Pattern.compile("[\\p{IsHan}A-Za-z0-9]+").matcher(normalized);
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.length() < 2) {
                continue;
            }
            keywordSet.add(token);
            // 对连续中文做二元切分，提高“玄幻爽文”等描述命中率
            if (containsHan(token) && token.length() >= 4) {
                for (int i = 0; i < token.length() - 1 && keywordSet.size() < MAX_KEYWORD_COUNT + 2;
                     i++) {
                    keywordSet.add(token.substring(i, i + 2));
                }
            }
        }
        return keywordSet.stream().limit(MAX_KEYWORD_COUNT).toList();
    }

    private boolean containsHan(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeScript script = Character.UnicodeScript.of(value.charAt(i));
            if (script == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private List<BookInfo> searchBooksFromDatabase(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return listHotBooks();
        }
        Map<Long, BookInfo> bookMap = new LinkedHashMap<>();
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (String keyword : keywords) {
            List<BookInfo> current = listBooksByKeyword(keyword);
            for (BookInfo book : current) {
                bookMap.putIfAbsent(book.getId(), book);
                scoreMap.merge(book.getId(), calculateScore(book, keyword), Integer::sum);
            }
        }

        if (bookMap.isEmpty()) {
            return listHotBooks();
        }

        List<BookInfo> sortedBooks = new ArrayList<>(bookMap.values());
        sortedBooks.sort(
            Comparator
                .comparingInt((BookInfo b) -> scoreMap.getOrDefault(b.getId(), 0))
                .thenComparingLong(b -> b.getVisitCount() == null ? 0L : b.getVisitCount())
                .reversed()
        );
        return sortedBooks;
    }

    private List<BookInfo> listBooksByKeyword(String keyword) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0);
        queryWrapper.and(wrapper -> wrapper
            .like(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, keyword)
            .or().like(COLUMN_AUTHOR_NAME, keyword)
            .or().like(COLUMN_CATEGORY_NAME, keyword)
            .or().like(COLUMN_BOOK_DESC, keyword));
        queryWrapper.orderByDesc(DatabaseConsts.BookTable.COLUMN_VISIT_COUNT)
            .orderByDesc(DatabaseConsts.BookTable.COLUMN_LAST_CHAPTER_UPDATE_TIME)
            .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
        return bookInfoMapper.selectList(queryWrapper);
    }

    private int calculateScore(BookInfo book, String keyword) {
        int score = 1;
        if (contains(book.getBookName(), keyword)) {
            score += 5;
        }
        if (contains(book.getCategoryName(), keyword)) {
            score += 4;
        }
        if (contains(book.getAuthorName(), keyword)) {
            score += 3;
        }
        if (contains(book.getBookDesc(), keyword)) {
            score += 2;
        }
        return score;
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source) && StringUtils.hasText(keyword) && source.contains(keyword);
    }

    private List<BookInfo> listHotBooks() {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
            .orderByDesc(DatabaseConsts.BookTable.COLUMN_VISIT_COUNT)
            .orderByDesc(DatabaseConsts.BookTable.COLUMN_LAST_CHAPTER_UPDATE_TIME)
            .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
        return bookInfoMapper.selectList(queryWrapper);
    }

    private FrontAiChatRespDto.BookItem convertBookItem(BookInfo book) {
        return FrontAiChatRespDto.BookItem.builder()
            .id(book.getId())
            .bookName(book.getBookName())
            .authorName(book.getAuthorName())
            .categoryName(book.getCategoryName())
            .detailUrl("/#/book/" + book.getId())
            .build();
    }

    private String buildAnswer(FrontAiChatReqDto dto, List<FrontAiChatRespDto.BookItem> books) {
        if (CollectionUtils.isEmpty(books)) {
            return "暂时没有找到符合条件的小说，你可以换几个关键词再试试。";
        }
        String aiAnswer = generateAnswerByModel(dto, books);
        if (StringUtils.hasText(aiAnswer)) {
            return aiAnswer;
        }
        return buildFallbackAnswer(books);
    }

    private String generateAnswerByModel(FrontAiChatReqDto dto, List<FrontAiChatRespDto.BookItem> books) {
        String prompt = buildPrompt(dto, books);
        try {
            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        } catch (Exception exception) {
            if (exception instanceof RestClientResponseException responseException) {
                log.warn("AI 找书生成回复失败，status={}, body={}",
                    responseException.getStatusCode(),
                    responseException.getResponseBodyAsString(),
                    responseException);
            } else {
                log.warn("AI 找书生成回复失败，使用兜底文案。异常信息: {}",
                    exception.getMessage(), exception);
            }
            return null;
        }
    }

    private String buildPrompt(FrontAiChatReqDto dto, List<FrontAiChatRespDto.BookItem> books) {
        String historyText = formatHistory(dto.getHistory());
        String bookText = books.stream()
            .map(item -> String.format("《%s》 作者:%s 分类:%s 链接:%s",
                item.getBookName(),
                defaultText(item.getAuthorName()),
                defaultText(item.getCategoryName()),
                item.getDetailUrl()))
            .collect(Collectors.joining("\n"));

        return """
            你是在线小说网站的找书助手，请基于用户需求和候选书单给出推荐回复。
            要求：
            1. 只能推荐候选书单中的书，不要编造。
            2. 回复用中文，控制在 2-4 句，风格自然。
            3. 最后提醒用户可点击下方链接进入详情页。

            对话历史：
            %s

            用户当前需求：
            %s

            候选书单：
            %s
            """.formatted(historyText, dto.getMessage(), bookText);
    }

    private String formatHistory(List<FrontAiChatReqDto.HistoryItem> history) {
        if (CollectionUtils.isEmpty(history)) {
            return "无";
        }
        return history.stream()
            .filter(item -> item != null && StringUtils.hasText(item.getContent()))
            .limit(6)
            .map(item -> String.format("%s: %s",
                defaultText(item.getRole()),
                item.getContent()))
            .collect(Collectors.joining(" | "));
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value : "未知";
    }

    private String buildFallbackAnswer(List<FrontAiChatRespDto.BookItem> books) {
        String bookNames = books.stream()
            .map(book -> "《" + book.getBookName() + "》")
            .collect(Collectors.joining("、"));
        return "根据你的描述，我先从站内库筛选出：" + bookNames + "。你可以点击下方链接查看详情页。";
    }
}
