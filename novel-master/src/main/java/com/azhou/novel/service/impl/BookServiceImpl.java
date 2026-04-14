package com.azhou.novel.service.impl;

import com.azhou.novel.core.annotation.Key;
import com.azhou.novel.core.annotation.Lock;
import com.azhou.novel.core.auth.UserHolder;
import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.dao.entity.AuthorUploadRecord;
import com.azhou.novel.dao.entity.BookChapter;
import com.azhou.novel.dao.entity.BookComment;
import com.azhou.novel.dao.entity.BookContent;
import com.azhou.novel.dao.entity.BookInfo;
import com.azhou.novel.dao.entity.UserInfo;
import com.azhou.novel.dao.mapper.AuthorUploadRecordMapper;
import com.azhou.novel.dao.mapper.BookChapterMapper;
import com.azhou.novel.dao.mapper.BookCommentMapper;
import com.azhou.novel.dao.mapper.BookContentMapper;
import com.azhou.novel.dao.mapper.BookInfoMapper;
import com.azhou.novel.dao.mapper.UserInfoMapper;
import com.azhou.novel.dto.AuthorInfoDto;
import com.azhou.novel.dto.req.BookAddReqDto;
import com.azhou.novel.dto.req.BookUploadReqDto;
import com.azhou.novel.dto.req.ChapterAddReqDto;
import com.azhou.novel.dto.req.ChapterUpdateReqDto;
import com.azhou.novel.dto.req.UserCommentReqDto;
import com.azhou.novel.dto.resp.AuthorUploadRecordRespDto;
import com.azhou.novel.dto.resp.BookCategoryRespDto;
import com.azhou.novel.dto.resp.BookChapterAboutRespDto;
import com.azhou.novel.dto.resp.BookChapterRespDto;
import com.azhou.novel.dto.resp.BookCommentRespDto;
import com.azhou.novel.dto.resp.BookContentAboutRespDto;
import com.azhou.novel.dto.resp.BookInfoRespDto;
import com.azhou.novel.dto.resp.BookRankRespDto;
import com.azhou.novel.dto.resp.ChapterContentRespDto;
import com.azhou.novel.dto.resp.UserCommentRespDto;
import com.azhou.novel.manager.cache.AuthorInfoCacheManager;
import com.azhou.novel.manager.cache.BookCategoryCacheManager;
import com.azhou.novel.manager.cache.BookChapterCacheManager;
import com.azhou.novel.manager.cache.BookContentCacheManager;
import com.azhou.novel.manager.cache.BookInfoCacheManager;
import com.azhou.novel.manager.cache.BookRankCacheManager;
import com.azhou.novel.manager.dao.UserDaoManager;
import com.azhou.novel.manager.mq.AmqpMsgManager;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.azhou.novel.service.BookService;
import com.azhou.novel.service.UserService;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 小说模块 服务实现类
 *
 * @author azhou
 * @date 2026/03/10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookRankCacheManager bookRankCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

    private final BookCommentMapper bookCommentMapper;

    private final UserInfoMapper userInfoMapper;

    private final AuthorUploadRecordMapper authorUploadRecordMapper;

    private final UserDaoManager userDaoManager;

    private final AmqpMsgManager amqpMsgManager;

    private final UserService userService;

    private static final Integer REC_BOOK_COUNT = 4;
    private static final Integer DEFAULT_TAG_CLOUD_SIZE = 30;
    private static final Integer UPLOAD_PERMISSION_ENABLED = 1;
    private static final Integer UPLOAD_STATUS_SUCCESS = 1;
    private static final String UPLOAD_SPLIT_RULE = "chapter_regex";
    private static final Pattern CHAPTER_TITLE_PATTERN = Pattern.compile(
        "^(第\\s*[0-9零一二三四五六七八九十百千万两〇]+\\s*[章回节卷部篇].*|chapter\\s*\\d+.*)$",
        Pattern.CASE_INSENSITIVE);

    @Override
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return RestResp.ok(bookRankCacheManager.listVisitRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return RestResp.ok(bookRankCacheManager.listNewestRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return RestResp.ok(bookRankCacheManager.listUpdateRankBooks());
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listTagCloudBooks(Integer size) {
        // 标签云推荐策略：
        // 1) 偏好推荐和随机推荐强制混合，避免刷新只改样式不换书；
        // 2) 默认偏好占比 65%，随机占比 35%；
        // 3) 结果最终再随机打散，确保视觉与内容都变化。
        int targetSize = (size == null || size <= 0) ? DEFAULT_TAG_CLOUD_SIZE : Math.min(size, 60);
        Long userId = UserHolder.getUserId();

        List<String> preferTags = listUserPreferTags(userId);
        int preferredQuota = CollectionUtils.isEmpty(preferTags) ? 0 : Math.max(6, (int) Math.round(targetSize * 0.65));
        preferredQuota = Math.min(preferredQuota, targetSize);
        int randomQuota = targetSize - preferredQuota;

        LinkedHashMap<Long, BookInfo> finalBooks = new LinkedHashMap<>();

        List<BookInfo> preferredBooks = Collections.emptyList();
        if (!CollectionUtils.isEmpty(preferTags)) {
            preferredBooks = bookInfoMapper.listBooksByCategoryNames(preferTags, targetSize * 3);
            Collections.shuffle(preferredBooks);
            preferredBooks.stream()
                .limit(preferredQuota)
                .forEach(book -> finalBooks.put(book.getId(), book));
        }

        List<BookInfo> randomBooks = bookInfoMapper.listRandomBooks(targetSize * 4);
        randomBooks.stream()
            .filter(book -> !finalBooks.containsKey(book.getId()))
            .limit(randomQuota)
            .forEach(book -> finalBooks.put(book.getId(), book));

        if (finalBooks.size() < targetSize) {
            preferredBooks.stream()
                .filter(book -> !finalBooks.containsKey(book.getId()))
                .forEach(book -> finalBooks.put(book.getId(), book));
        }
        if (finalBooks.size() < targetSize) {
            randomBooks.stream()
                .filter(book -> !finalBooks.containsKey(book.getId()))
                .forEach(book -> finalBooks.put(book.getId(), book));
        }

        List<BookInfo> mixedBooks = new ArrayList<>(finalBooks.values());
        Collections.shuffle(mixedBooks);
        List<BookInfoRespDto> resp = mixedBooks.stream()
            .limit(targetSize)
            .map(this::toBookInfoRespDto)
            .toList();
        return RestResp.ok(resp);
    }

    @Override
    public RestResp<BookInfoRespDto> getBookById(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }

    @Override
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId) {
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);

        // 查询最新章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(
            bookInfo.getLastChapterId());

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(bookInfo.getLastChapterId());

        // 查询章节总数
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        Long chapterTotal = bookChapterMapper.selectCount(chapterQueryWrapper);

        // 组装数据并返回
        return RestResp.ok(BookChapterAboutRespDto.builder()
            .chapterInfo(bookChapter)
            .chapterTotal(chapterTotal)
            .contentSummary(content.substring(0, 30))
            .build());
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId)
        throws NoSuchAlgorithmException {
        Long categoryId = bookInfoCacheManager.getBookInfo(bookId).getCategoryId();
        List<Long> lastUpdateIdList = bookInfoCacheManager.getLastUpdateIdList(categoryId);

        // 检查列表是否为空或不足
        if (CollectionUtils.isEmpty(lastUpdateIdList)) {
            return RestResp.ok(Collections.emptyList());
        }

        // 排除当前书籍，同时确保有足够的推荐书籍用于展示
        List<Long> candidateIdList = lastUpdateIdList.stream()
                .filter(id -> !Objects.equals(id, bookId))
                .toList();

        if (candidateIdList.isEmpty()) {
            return RestResp.ok(Collections.emptyList());
        }

        // 确定实际推荐的书籍数量
        int actualRecCount = Math.min(REC_BOOK_COUNT, candidateIdList.size());

        List<BookInfoRespDto> respDtoList = new ArrayList<>();
        Set<Integer> recIdIndexSet = new HashSet<>();
        Random rand = SecureRandom.getInstanceStrong();

        // 使用 Set 提高查找效率，同时修复bug防止无限循环
        while (respDtoList.size() < actualRecCount && recIdIndexSet.size() < candidateIdList.size()) {
            int recIdIndex = rand.nextInt(candidateIdList.size());
            if (!recIdIndexSet.contains(recIdIndex)) {
                recIdIndexSet.add(recIdIndex);
                Long recBookId = candidateIdList.get(recIdIndex);
                BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(recBookId);
                respDtoList.add(bookInfo);
            }
        }

        return RestResp.ok(respDtoList);
    }

    @Override
    public RestResp<Void> addVisitCount(Long bookId) {
        bookInfoMapper.addVisitCount(bookId);
        return RestResp.ok();
    }

    @Override
    public RestResp<Long> getPreChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询上一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .lt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
            Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                .map(BookChapter::getId)
                .orElse(null)
        );
    }

    @Override
    public RestResp<Long> getNextChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询下一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .gt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
            .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
            Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                .map(BookChapter::getId)
                .orElse(null)
        );
    }

    @Override
    public RestResp<List<BookChapterRespDto>> listChapters(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        return RestResp.ok(bookChapterMapper.selectList(queryWrapper).stream()
            .map(v -> BookChapterRespDto.builder()
                .id(v.getId())
                .chapterName(v.getChapterName())
                .isVip(v.getIsVip())
                .build()).toList());
    }

    @Override
    public RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.listCategory(workDirection));
    }

    @Lock(prefix = "userComment")
    @Override
    public RestResp<Void> saveComment(
        @Key(expr = "#{userId + '::' + bookId}") UserCommentReqDto dto) {
        // 校验书籍是否存在
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        // 校验用户是否已发表评论
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId())
            .eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, dto.getBookId());
        if (bookCommentMapper.selectCount(queryWrapper) > 0) {
            // 用户已发表评论
            return RestResp.fail(ErrorCodeEnum.USER_COMMENTED);
        }
        BookComment bookComment = new BookComment();
        bookComment.setBookId(dto.getBookId());
        bookComment.setUserId(dto.getUserId());
        bookComment.setCommentContent(dto.getCommentContent());
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);
        return RestResp.ok();
    }

    @Override
    public RestResp<BookCommentRespDto> listNewestComments(Long bookId) {
        // 查询评论总数
        QueryWrapper<BookComment> commentCountQueryWrapper = new QueryWrapper<>();
        commentCountQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        Long commentTotal = bookCommentMapper.selectCount(commentCountQueryWrapper);
        BookCommentRespDto bookCommentRespDto = BookCommentRespDto.builder()
            .commentTotal(commentTotal).build();
        if (commentTotal > 0) {

            // 查询最新的评论列表
            QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName())
                .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
            List<BookComment> bookComments = bookCommentMapper.selectList(commentQueryWrapper);

            // 查询评论用户信息，并设置需要返回的评论用户名
            List<Long> userIds = bookComments.stream().map(BookComment::getUserId).toList();
            List<UserInfo> userInfos = userDaoManager.listUsers(userIds);
            Map<Long, UserInfo> userInfoMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfo::getId, Function.identity()));
            List<BookCommentRespDto.CommentInfo> commentInfos = bookComments.stream()
                .map(v -> BookCommentRespDto.CommentInfo.builder()
                    .id(v.getId())
                    .commentUserId(v.getUserId())
                    .commentUser(userInfoMap.get(v.getUserId()).getUsername())
                    .commentUserPhoto(userInfoMap.get(v.getUserId()).getUserPhoto())
                    .commentContent(v.getCommentContent())
                    .commentTime(v.getCreateTime()).build()).toList();
            bookCommentRespDto.setComments(commentInfos);
        } else {
            bookCommentRespDto.setComments(Collections.emptyList());
        }
        return RestResp.ok(bookCommentRespDto);
    }

    @Override
    public RestResp<Void> deleteComment(Long userId, Long commentId) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), commentId)
            .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        bookCommentMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateComment(Long userId, Long id, String content) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
            .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId);
        BookComment bookComment = new BookComment();
        bookComment.setCommentContent(content);
        bookCommentMapper.update(bookComment, queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> saveBook(BookAddReqDto dto) {
        // 校验小说名是否已存在
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, dto.getBookName());
        if (bookInfoMapper.selectCount(queryWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NAME_EXIST);
        }
        BookInfo bookInfo = new BookInfo();
        // 设置作家信息
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        bookInfo.setAuthorId(author.getId());
        bookInfo.setAuthorName(author.getPenName());
        // 设置其他信息
        bookInfo.setWorkDirection(dto.getWorkDirection());
        bookInfo.setCategoryId(dto.getCategoryId());
        bookInfo.setCategoryName(dto.getCategoryName());
        bookInfo.setBookName(dto.getBookName());
        bookInfo.setPicUrl(dto.getPicUrl());
        bookInfo.setBookDesc(dto.getBookDesc());
        bookInfo.setIsVip(dto.getIsVip());
        bookInfo.setScore(0);
        bookInfo.setCreateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());
        // 保存小说信息
        bookInfoMapper.insert(bookInfo);
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> saveBookChapter(ChapterAddReqDto dto) {
        // 校验该作品是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (!Objects.equals(bookInfo.getAuthorId(), UserHolder.getAuthorId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        // 1) 保存章节相关信息到小说章节表
        //  a) 查询最新章节号
        int chapterNum = 0;
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter bookChapter = bookChapterMapper.selectOne(chapterQueryWrapper);
        if (Objects.nonNull(bookChapter)) {
            chapterNum = bookChapter.getChapterNum() + 1;
        }
        //  b) 设置章节相关信息并保存
        BookChapter newBookChapter = new BookChapter();
        newBookChapter.setBookId(dto.getBookId());
        newBookChapter.setChapterName(dto.getChapterName());
        newBookChapter.setChapterNum(chapterNum);
        newBookChapter.setWordCount(dto.getChapterContent().length());
        newBookChapter.setIsVip(dto.getIsVip());
        newBookChapter.setCreateTime(LocalDateTime.now());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.insert(newBookChapter);

        // 2) 保存章节内容到小说内容表
        BookContent bookContent = new BookContent();
        bookContent.setContent(dto.getChapterContent());
        bookContent.setChapterId(newBookChapter.getId());
        bookContent.setCreateTime(LocalDateTime.now());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.insert(bookContent);

        // 3) 更新小说表最新章节信息和小说总字数信息
        //  a) 更新小说表关于最新章节的信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(dto.getBookId());
        newBookInfo.setLastChapterId(newBookChapter.getId());
        newBookInfo.setLastChapterName(newBookChapter.getChapterName());
        newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() + newBookChapter.getWordCount());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(newBookInfo);
        //  b) 清除小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(dto.getBookId());
        //  c) 发送小说信息更新的 MQ 消息
        amqpMsgManager.sendBookChangeMsg(dto.getBookId());
        return RestResp.ok();
    }

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(PageReqDto dto) {
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, UserHolder.getAuthorId())
            .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookInfo> bookInfoPage = bookInfoMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
            bookInfoPage.getRecords().stream().map(v -> BookInfoRespDto.builder()
                .id(v.getId())
                .bookName(v.getBookName())
                .picUrl(v.getPicUrl())
                .categoryName(v.getCategoryName())
                .wordCount(v.getWordCount())
                .visitCount(v.getVisitCount())
                .updateTime(v.getUpdateTime())
                .build()).toList()));
    }

    @Override
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(Long bookId, PageReqDto dto) {
        IPage<BookChapter> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        IPage<BookChapter> bookChapterPage = bookChapterMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
            bookChapterPage.getRecords().stream().map(v -> BookChapterRespDto.builder()
                .id(v.getId())
                .chapterName(v.getChapterName())
                .chapterUpdateTime(v.getUpdateTime())
                .isVip(v.getIsVip())
                .build()).toList()));
    }

    @Override
    public RestResp<PageRespDto<UserCommentRespDto>> listComments(Long userId, PageReqDto pageReqDto) {
        IPage<BookComment> page = new Page<>();
        page.setCurrent(pageReqDto.getPageNum());
        page.setSize(pageReqDto.getPageSize());
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, userId)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        IPage<BookComment> bookCommentPage = bookCommentMapper.selectPage(page, queryWrapper);
        List<BookComment> comments = bookCommentPage.getRecords();
        
        List<UserCommentRespDto> commentRespDtoList = Collections.emptyList();
        if (!CollectionUtils.isEmpty(comments)) {
            List<Long> bookIds = comments.stream().map(BookComment::getBookId).toList();
            QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
            bookInfoQueryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIds);
            Map<Long, BookInfo> bookInfoMap = bookInfoMapper.selectList(bookInfoQueryWrapper).stream()
                .collect(Collectors.toMap(BookInfo::getId, Function.identity()));
            
            commentRespDtoList = comments.stream().map(v -> {
                BookInfo bookInfo = bookInfoMap.get(v.getBookId());
                return UserCommentRespDto.builder()
                    .commentContent(v.getCommentContent())
                    .commentBook(bookInfo != null ? bookInfo.getBookName() : null)
                    .commentBookPic(bookInfo != null ? bookInfo.getPicUrl() : null)
                    .commentTime(v.getCreateTime())
                    .build();
            }).toList();
        }
        
        return RestResp.ok(PageRespDto.of(pageReqDto.getPageNum(), pageReqDto.getPageSize(), 
            page.getTotal(), commentRespDtoList));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> deleteBookChapter(Long chapterId) {
        // 1.查询章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 2.查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(chapter.getBookId());
        // 3.删除章节信息
        bookChapterMapper.deleteById(chapterId);
        // 4.删除章节内容
        QueryWrapper<BookContent> bookContentQueryWrapper = new QueryWrapper<>();
        bookContentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.delete(bookContentQueryWrapper);
        // 5.更新小说信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(chapter.getBookId());
        newBookInfo.setUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() - chapter.getChapterWordCount());
        if (Objects.equals(bookInfo.getLastChapterId(), chapterId)) {
            // 设置最新章节信息
            QueryWrapper<BookChapter> bookChapterQueryWrapper = new QueryWrapper<>();
            bookChapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, chapter.getBookId())
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
            BookChapter bookChapter = bookChapterMapper.selectOne(bookChapterQueryWrapper);
            Long lastChapterId = 0L;
            String lastChapterName = "";
            LocalDateTime lastChapterUpdateTime = null;
            if (Objects.nonNull(bookChapter)) {
                lastChapterId = bookChapter.getId();
                lastChapterName = bookChapter.getChapterName();
                lastChapterUpdateTime = bookChapter.getUpdateTime();
            }
            newBookInfo.setLastChapterId(lastChapterId);
            newBookInfo.setLastChapterName(lastChapterName);
            newBookInfo.setLastChapterUpdateTime(lastChapterUpdateTime);
        }
        bookInfoMapper.updateById(newBookInfo);
        // 6.清理章节信息缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        // 7.清理章节内容缓存
        bookContentCacheManager.evictBookContentCache(chapterId);
        // 8.清理小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(chapter.getBookId());
        // 9.发送小说信息更新的 MQ 消息
        amqpMsgManager.sendBookChangeMsg(chapter.getBookId());
        return RestResp.ok();
    }

    @Override
    public RestResp<ChapterContentRespDto> getBookChapter(Long chapterId) {
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        String bookContent = bookContentCacheManager.getBookContent(chapterId);
        return RestResp.ok(
            ChapterContentRespDto.builder()
                .chapterName(chapter.getChapterName())
                .chapterContent(bookContent)
                .isVip(chapter.getIsVip())
                .build());
    }

    @Transactional
    @Override
    public RestResp<Void> updateBookChapter(Long chapterId, ChapterUpdateReqDto dto) {
        // 1.查询章节信息
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        // 2.查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(chapter.getBookId());
        // 3.更新章节信息
        BookChapter newChapter = new BookChapter();
        newChapter.setId(chapterId);
        newChapter.setChapterName(dto.getChapterName());
        newChapter.setWordCount(dto.getChapterContent().length());
        newChapter.setIsVip(dto.getIsVip());
        newChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.updateById(newChapter);
        // 4.更新章节内容
        BookContent newContent = new BookContent();
        newContent.setContent(dto.getChapterContent());
        newContent.setUpdateTime(LocalDateTime.now());
        QueryWrapper<BookContent> bookContentQueryWrapper = new QueryWrapper<>();
        bookContentQueryWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.update(newContent, bookContentQueryWrapper);
        // 5.更新小说信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(chapter.getBookId());
        newBookInfo.setUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(
            bookInfo.getWordCount() - chapter.getChapterWordCount() + dto.getChapterContent().length());
        if (Objects.equals(bookInfo.getLastChapterId(), chapterId)) {
            // 更新最新章节信息
            newBookInfo.setLastChapterName(dto.getChapterName());
            newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        }
        bookInfoMapper.updateById(newBookInfo);
        // 6.清理章节信息缓存
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        // 7.清理章节内容缓存
        bookContentCacheManager.evictBookContentCache(chapterId);
        // 8.清理小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(chapter.getBookId());
        // 9.发送小说信息更新的 MQ 消息
        amqpMsgManager.sendBookChangeMsg(chapter.getBookId());
        return RestResp.ok();
    }

    /**
     * 上传TXT并拆分入库
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> uploadBook(BookUploadReqDto dto, MultipartFile file) {
        // 校验上传权限，避免普通作者误调用上传接口。
        if (!hasUploadPermission(UserHolder.getUserId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        if (file == null || file.isEmpty()) {
            return RestResp.fail(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR);
        }
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName) || !fileName.toLowerCase().endsWith(".txt")) {
            return RestResp.fail(ErrorCodeEnum.USER_UPLOAD_FILE_TYPE_NOT_MATCH);
        }

        // 校验小说名是否存在，保持与现有发布逻辑一致。
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, dto.getBookName());
        if (bookInfoMapper.selectCount(queryWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NAME_EXIST);
        }

        AuthorInfoDto author = authorInfoCacheManager.getAuthor(UserHolder.getUserId());
        if (author == null) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }

        long startTs = System.currentTimeMillis();
        DecodedTxt decodedTxt;
        List<ChapterDraft> chapterDrafts;
        try {
            decodedTxt = decodeTxt(file);
            chapterDrafts = splitChapters(decodedTxt.content());
        } catch (Exception e) {
            log.error("TXT解析失败, fileName={}", fileName, e);
            return RestResp.fail(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR);
        }
        if (CollectionUtils.isEmpty(chapterDrafts)) {
            return RestResp.fail(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR);
        }

        // 小说简介按“手填优先，空时取第一章前6行”策略。
        String finalDesc = StringUtils.hasText(dto.getBookDesc())
            ? dto.getBookDesc()
            : extractSummary(chapterDrafts.get(0).rawContent(), 6);

        LocalDateTime now = LocalDateTime.now();
        String finalAuthorName = dto.getAuthorName() == null ? null : dto.getAuthorName().trim();
        BookInfo bookInfo = new BookInfo();
        bookInfo.setAuthorId(author.getId());
        bookInfo.setAuthorName(finalAuthorName);
        bookInfo.setWorkDirection(dto.getWorkDirection());
        bookInfo.setCategoryId(dto.getCategoryId());
        bookInfo.setCategoryName(dto.getCategoryName());
        bookInfo.setBookName(dto.getBookName());
        bookInfo.setPicUrl(dto.getPicUrl());
        bookInfo.setBookDesc(finalDesc);
        bookInfo.setIsVip(dto.getIsVip());
        bookInfo.setScore(0);
        bookInfo.setCreateTime(now);
        bookInfo.setUpdateTime(now);
        bookInfoMapper.insert(bookInfo);

        int wordTotal = 0;
        Long lastChapterId = null;
        String lastChapterName = null;
        LocalDateTime lastUpdateTime = null;
        for (int i = 0; i < chapterDrafts.size(); i++) {
            ChapterDraft chapterDraft = chapterDrafts.get(i);
            BookChapter chapter = new BookChapter();
            chapter.setBookId(bookInfo.getId());
            chapter.setChapterNum(i);
            chapter.setChapterName(chapterDraft.chapterName());
            chapter.setWordCount(chapterDraft.wordCount());
            chapter.setIsVip(dto.getIsVip());
            chapter.setCreateTime(now);
            chapter.setUpdateTime(now);
            bookChapterMapper.insert(chapter);

            BookContent content = new BookContent();
            content.setChapterId(chapter.getId());
            content.setContent(formatChapterContent(chapterDraft.rawContent()));
            content.setCreateTime(now);
            content.setUpdateTime(now);
            bookContentMapper.insert(content);

            wordTotal += chapterDraft.wordCount();
            lastChapterId = chapter.getId();
            lastChapterName = chapter.getChapterName();
            lastUpdateTime = chapter.getUpdateTime();
        }

        BookInfo updateBookInfo = new BookInfo();
        updateBookInfo.setId(bookInfo.getId());
        updateBookInfo.setWordCount(wordTotal);
        updateBookInfo.setLastChapterId(lastChapterId);
        updateBookInfo.setLastChapterName(lastChapterName);
        updateBookInfo.setLastChapterUpdateTime(lastUpdateTime);
        updateBookInfo.setUpdateTime(now);
        bookInfoMapper.updateById(updateBookInfo);

        // 记录上传结果，方便前端展示“拆分了多少章节”。
        AuthorUploadRecord uploadRecord = new AuthorUploadRecord();
        uploadRecord.setUserId(UserHolder.getUserId());
        uploadRecord.setAuthorId(author.getId());
        uploadRecord.setBookId(bookInfo.getId());
        uploadRecord.setBookName(dto.getBookName());
        uploadRecord.setPicUrl(dto.getPicUrl());
        uploadRecord.setTxtFileName(fileName);
        uploadRecord.setTxtFileSize(file.getSize());
        uploadRecord.setTxtCharset(decodedTxt.charset());
        uploadRecord.setChapterTotal(chapterDrafts.size());
        uploadRecord.setWordTotal(wordTotal);
        uploadRecord.setSplitRule(UPLOAD_SPLIT_RULE);
        uploadRecord.setDurationMs(System.currentTimeMillis() - startTs);
        uploadRecord.setStatus(UPLOAD_STATUS_SUCCESS);
        uploadRecord.setErrorMessage(null);
        uploadRecord.setCreateTime(now);
        uploadRecord.setUpdateTime(now);
        authorUploadRecordMapper.insert(uploadRecord);

        // 清理缓存并发送小说变更消息。
        bookInfoCacheManager.evictBookInfoCache(bookInfo.getId());
        amqpMsgManager.sendBookChangeMsg(bookInfo.getId());
        return RestResp.ok();
    }

    /**
     * 作者上传记录分页查询
     */
    @Override
    public RestResp<PageRespDto<AuthorUploadRecordRespDto>> listUploadRecords(PageReqDto dto) {
        IPage<AuthorUploadRecord> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<AuthorUploadRecord> queryWrapper = new QueryWrapper<>();
        // 展示全量上传记录，按时间倒序查看平台上传情况。
        queryWrapper.orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<AuthorUploadRecord> uploadRecordPage = authorUploadRecordMapper.selectPage(page,
            queryWrapper);
        List<Long> bookIds = uploadRecordPage.getRecords().stream()
            .map(AuthorUploadRecord::getBookId)
            .filter(Objects::nonNull)
            .toList();
        Map<Long, String> bookAuthorMapTemp = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(bookIds)) {
            QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
            bookInfoQueryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIds);
            bookAuthorMapTemp = bookInfoMapper.selectList(bookInfoQueryWrapper).stream()
                .collect(Collectors.toMap(BookInfo::getId, BookInfo::getAuthorName, (a, b) -> a));
        }
        final Map<Long, String> bookAuthorMap = bookAuthorMapTemp;
        List<AuthorUploadRecordRespDto> list = uploadRecordPage.getRecords().stream()
            .map(v -> AuthorUploadRecordRespDto.builder()
                .id(v.getId())
                .userId(v.getUserId())
                .bookId(v.getBookId())
                .bookName(v.getBookName())
                .authorName(bookAuthorMap.get(v.getBookId()))
                .txtFileName(v.getTxtFileName())
                .txtCharset(v.getTxtCharset())
                .chapterTotal(v.getChapterTotal())
                .wordTotal(v.getWordTotal())
                .durationMs(v.getDurationMs())
                .status(v.getStatus())
                .errorMessage(v.getErrorMessage())
                .createTime(v.getCreateTime())
                .build())
            .toList();
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
            list));
    }

    /**
     * 查询当前用户上传权限
     */
    @Override
    public RestResp<Integer> getUploadPermission() {
        return RestResp.ok(hasUploadPermission(UserHolder.getUserId()) ? 1 : 0);
    }

    @Override
    public RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId) {
        log.debug("userId:{}", UserHolder.getUserId());
        // 查询章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(chapterId);

        // 已在书架中的用户，在阅读时自动刷新章节进度
        userService.updateBookshelfReadProgress(
            UserHolder.getUserId(), bookChapter.getBookId(), chapterId);

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(chapterId);

        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookChapter.getBookId());

        // 组装数据并返回
        return RestResp.ok(BookContentAboutRespDto.builder()
            .bookInfo(bookInfo)
            .chapterInfo(bookChapter)
            .bookContent(content)
            .build());
    }

    /**
     * 校验用户是否具备上传权限
     */
    private boolean hasUploadPermission(Long userId) {
        if (userId == null) {
            return false;
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return userInfo != null && Objects.equals(userInfo.getCanUploadNovel(),
            UPLOAD_PERMISSION_ENABLED);
    }

    /**
     * 解码TXT内容，按UTF-8优先再兼容GBK/GB18030
     */
    private DecodedTxt decodeTxt(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("读取TXT文件失败", e);
        }
        List<Charset> charsets = List.of(StandardCharsets.UTF_8, Charset.forName("GBK"),
            Charset.forName("GB18030"));
        for (Charset charset : charsets) {
            try {
                CharsetDecoder decoder = charset.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(bytes));
                return new DecodedTxt(charset.name(), charBuffer.toString());
            } catch (Exception ignored) {
                // 尝试下一个编码。
            }
        }
        throw new IllegalStateException("TXT编码无法识别");
    }

    /**
     * 按章节标题拆分正文，未命中时降级为单章
     */
    private List<ChapterDraft> splitChapters(String content) {
        String normalized = normalizeLineBreak(content);
        List<ChapterDraft> chapterDrafts = new ArrayList<>();
        String currentTitle = null;
        StringBuilder currentContent = new StringBuilder();
        for (String line : normalized.split("\n", -1)) {
            String trimLine = line == null ? "" : line.trim();
            Matcher matcher = CHAPTER_TITLE_PATTERN.matcher(trimLine);
            if (StringUtils.hasText(trimLine) && matcher.matches()) {
                if (currentTitle != null) {
                    chapterDrafts.add(buildChapterDraft(currentTitle, currentContent.toString()));
                }
                currentTitle = trimLine;
                currentContent = new StringBuilder();
                continue;
            }
            currentContent.append(line).append("\n");
        }
        if (currentTitle != null) {
            chapterDrafts.add(buildChapterDraft(currentTitle, currentContent.toString()));
        }
        if (!CollectionUtils.isEmpty(chapterDrafts)) {
            return chapterDrafts;
        }
        String fallbackText = normalized.trim();
        if (!StringUtils.hasText(fallbackText)) {
            return Collections.emptyList();
        }
        chapterDrafts.add(buildChapterDraft("第一章 正文", fallbackText));
        return chapterDrafts;
    }

    /**
     * 构建章节草稿，统一章节名和字数
     */
    private ChapterDraft buildChapterDraft(String chapterName, String rawContent) {
        String cleanContent = StringUtils.hasText(rawContent) ? rawContent.trim() : "暂无正文";
        return new ChapterDraft(chapterName, cleanContent, cleanContent.length());
    }

    /**
     * 格式化章节正文，保持与现有库一致的HTML换行与缩进风格
     */
    private String formatChapterContent(String rawText) {
        String normalized = normalizeLineBreak(rawText);
        String escaped = HtmlUtils.htmlEscape(normalized, StandardCharsets.UTF_8.name());
        List<String> paragraphs = Arrays.stream(escaped.split("\n", -1))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
        if (CollectionUtils.isEmpty(paragraphs)) {
            return "&nbsp;&nbsp;&nbsp;&nbsp;暂无正文";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (i > 0) {
                builder.append("<br/><br/>");
            }
            builder.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(paragraphs.get(i));
        }
        return builder.toString();
    }

    /**
     * 提取简介摘要
     */
    private String extractSummary(String firstChapterText, int lineCount) {
        if (!StringUtils.hasText(firstChapterText)) {
            return "暂无简介";
        }
        String normalized = normalizeLineBreak(firstChapterText);
        List<String> lines = Arrays.stream(normalized.split("\n", -1))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .limit(Math.max(lineCount, 1))
            .toList();
        if (CollectionUtils.isEmpty(lines)) {
            return "暂无简介";
        }
        String summary = String.join(" ", lines);
        return summary.length() > 2000 ? summary.substring(0, 2000) : summary;
    }

    /**
     * 统一换行符，避免Windows与Unix行尾差异影响拆分规则
     */
    private String normalizeLineBreak(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return content.replace("\r\n", "\n").replace("\r", "\n");
    }

    private List<String> listUserPreferTags(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null || !StringUtils.hasText(userInfo.getPreferTags())) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Arrays.stream(userInfo.getPreferTags().split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .forEach(tag -> expandPreferTag(tag, tags));
        return new ArrayList<>(tags);
    }

    private void expandPreferTag(String tag, Set<String> tags) {
        if ("男频".equals(tag)) {
            bookCategoryCacheManager.listCategory(0).stream()
                .map(BookCategoryRespDto::getName)
                .forEach(tags::add);
            return;
        }
        if ("女频".equals(tag)) {
            bookCategoryCacheManager.listCategory(1).stream()
                .map(BookCategoryRespDto::getName)
                .forEach(tags::add);
            return;
        }
        tags.add(tag);
    }

    private BookInfoRespDto toBookInfoRespDto(BookInfo book) {
        return BookInfoRespDto.builder()
            .id(book.getId())
            .bookName(book.getBookName())
            .categoryId(book.getCategoryId())
            .categoryName(book.getCategoryName())
            .authorId(book.getAuthorId())
            .authorName(book.getAuthorName())
            .bookDesc(book.getBookDesc())
            .picUrl(book.getPicUrl())
            .wordCount(book.getWordCount())
            .visitCount(book.getVisitCount())
            .lastChapterName(book.getLastChapterName())
            .build();
    }

    /**
     * TXT解码结果
     */
    private record DecodedTxt(String charset, String content) {
    }

    /**
     * 章节草稿
     */
    private record ChapterDraft(String chapterName, String rawContent, Integer wordCount) {
    }
}
