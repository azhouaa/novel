package com.azhou.novel.service.impl;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.dao.entity.AuthorInfo;
import com.azhou.novel.dao.entity.AuthorUploadRecord;
import com.azhou.novel.dao.entity.BookChapter;
import com.azhou.novel.dao.entity.BookComment;
import com.azhou.novel.dao.entity.BookContent;
import com.azhou.novel.dao.entity.BookInfo;
import com.azhou.novel.dao.entity.AuthorUploadRecord;
import com.azhou.novel.dao.entity.UserInfo;
import com.azhou.novel.dao.mapper.AuthorInfoMapper;
import com.azhou.novel.dao.mapper.AuthorUploadRecordMapper;
import com.azhou.novel.dao.mapper.BookChapterMapper;
import com.azhou.novel.dao.mapper.BookCommentMapper;
import com.azhou.novel.dao.mapper.BookContentMapper;
import com.azhou.novel.dao.mapper.BookInfoMapper;
import com.azhou.novel.dao.mapper.AuthorUploadRecordMapper;
import com.azhou.novel.dao.mapper.UserInfoMapper;
import com.azhou.novel.dto.resp.AdminAuditBookItemRespDto;
import com.azhou.novel.dto.resp.AdminAuditChapterItemRespDto;
import com.azhou.novel.dto.resp.AdminDashboardRespDto;
import com.azhou.novel.dto.resp.AdminCommentItemRespDto;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;
import com.azhou.novel.dto.resp.ChapterContentRespDto;
import com.azhou.novel.manager.cache.AuthorInfoCacheManager;
import com.azhou.novel.manager.cache.BookChapterCacheManager;
import com.azhou.novel.manager.cache.BookContentCacheManager;
import com.azhou.novel.manager.cache.BookInfoCacheManager;
import com.azhou.novel.manager.cache.UserInfoCacheManager;
import com.azhou.novel.manager.mq.AmqpMsgManager;
import com.azhou.novel.service.AdminService;
import com.azhou.novel.service.AuthorService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 管理后台服务实现。
 *
 * @author azhou
 * @date 2026/05/17
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Integer USER_STATUS_NORMAL = 0;
    private static final Integer USER_STATUS_BANNED = 1;
    private static final Integer AUTHOR_STATUS_NORMAL = 0;
    private static final Integer AUTHOR_STATUS_BANNED = 1;
    private static final Integer UPLOAD_PERMISSION_ENABLED = 1;
    private static final Integer UPLOAD_PERMISSION_DISABLED = 0;
    private static final Integer AUDIT_STATUS_PENDING = 0;
    private static final Integer AUDIT_STATUS_PASS = 1;
    private static final Integer AUDIT_STATUS_REJECT = 2;

    private final UserInfoMapper userInfoMapper;

    private final AuthorInfoMapper authorInfoMapper;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

    private final BookCommentMapper bookCommentMapper;

    private final AuthorUploadRecordMapper authorUploadRecordMapper;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final UserInfoCacheManager userInfoCacheManager;

    private final AmqpMsgManager amqpMsgManager;

    @Override
    public RestResp<PageRespDto<AdminUserItemRespDto>> listUsers(PageReqDto dto) {
        IPage<UserInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<UserInfo> userQuery = new QueryWrapper<>();
        userQuery.orderByDesc(DatabaseConsts.CommonColumnEnum.ID.getName());
        IPage<UserInfo> userPage = userInfoMapper.selectPage(page, userQuery);

        List<Long> userIds = userPage.getRecords().stream().map(UserInfo::getId).toList();
        Map<Long, AuthorInfo> authorMap = Map.of();
        if (!CollectionUtils.isEmpty(userIds)) {
            QueryWrapper<AuthorInfo> authorQuery = new QueryWrapper<>();
            authorQuery.in(DatabaseConsts.AuthorInfoTable.COLUMN_USER_ID, userIds);
            authorMap = authorInfoMapper.selectList(authorQuery).stream()
                .collect(Collectors.toMap(AuthorInfo::getUserId, v -> v, (a, b) -> a));
        }
        Map<Long, AuthorInfo> finalAuthorMap = authorMap;
        List<AdminUserItemRespDto> list = userPage.getRecords().stream().map(user -> {
            AuthorInfo authorInfo = finalAuthorMap.get(user.getId());
            return AdminUserItemRespDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickName(user.getNickName())
                .userStatus(user.getStatus())
                .isAdmin(Objects.equals(user.getIsAdmin(), 1) ? 1 : 0)
                .isAuthor(authorInfo == null ? 0 : 1)
                .authorStatus(authorInfo == null ? null : authorInfo.getStatus())
                .canUploadNovel(Objects.equals(user.getCanUploadNovel(), 1) ? 1 : 0)
                .createTime(user.getCreateTime())
                .build();
        }).toList();

        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), userPage.getTotal(), list));
    }

    @Override
    public RestResp<AdminDashboardRespDto> getDashboard() {
        Long totalUsers = userInfoMapper.selectCount(new QueryWrapper<>());
        Long totalAuthors = authorInfoMapper.selectCount(new QueryWrapper<>());
        Long totalBooks = bookInfoMapper.selectCount(new QueryWrapper<>());
        Long totalChapters = bookChapterMapper.selectCount(new QueryWrapper<>());
        Long totalComments = bookCommentMapper.selectCount(new QueryWrapper<>());

        QueryWrapper<BookInfo> pendingBookQuery = new QueryWrapper<>();
        pendingBookQuery.eq("audit_status", AUDIT_STATUS_PENDING);
        Long pendingBooks = bookInfoMapper.selectCount(pendingBookQuery);

        QueryWrapper<BookChapter> pendingChapterQuery = new QueryWrapper<>();
        pendingChapterQuery.eq("audit_status", AUDIT_STATUS_PENDING);
        Long pendingChapters = bookChapterMapper.selectCount(pendingChapterQuery);

        Long totalUploadRecords = authorUploadRecordMapper.selectCount(new QueryWrapper<>());

        return RestResp.ok(AdminDashboardRespDto.builder()
            .totalUsers(totalUsers)
            .totalAuthors(totalAuthors)
            .totalBooks(totalBooks)
            .totalChapters(totalChapters)
            .totalComments(totalComments)
            .pendingBooks(pendingBooks)
            .pendingChapters(pendingChapters)
            .totalUploadRecords(totalUploadRecords)
            .build());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> approveAuthor(Long userId) {
        AuthorInfo authorInfo = getAuthorByUserId(userId);
        if (authorInfo == null) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        AuthorInfo updateAuthor = new AuthorInfo();
        updateAuthor.setId(authorInfo.getId());
        updateAuthor.setStatus(AUTHOR_STATUS_NORMAL);
        updateAuthor.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.updateById(updateAuthor);

        UserInfo updateUser = new UserInfo();
        updateUser.setId(userId);
        updateUser.setCanUploadNovel(UPLOAD_PERMISSION_ENABLED);
        updateUser.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(updateUser);

        authorInfoCacheManager.evictAuthorCache();
        userInfoCacheManager.evictUserCache();
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> banUser(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            return RestResp.fail(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        UserInfo updateUser = new UserInfo();
        updateUser.setId(userId);
        updateUser.setStatus(USER_STATUS_BANNED);
        updateUser.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(updateUser);
        userInfoCacheManager.evictUserCache();
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> unbanUser(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            return RestResp.fail(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        UserInfo updateUser = new UserInfo();
        updateUser.setId(userId);
        updateUser.setStatus(USER_STATUS_NORMAL);
        updateUser.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(updateUser);
        userInfoCacheManager.evictUserCache();
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> revokeAuthor(Long userId) {
        AuthorInfo authorInfo = getAuthorByUserId(userId);
        if (authorInfo == null) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        AuthorInfo updateAuthor = new AuthorInfo();
        updateAuthor.setId(authorInfo.getId());
        updateAuthor.setStatus(AUTHOR_STATUS_BANNED);
        updateAuthor.setUpdateTime(LocalDateTime.now());
        authorInfoMapper.updateById(updateAuthor);

        UserInfo updateUser = new UserInfo();
        updateUser.setId(userId);
        updateUser.setCanUploadNovel(UPLOAD_PERMISSION_DISABLED);
        updateUser.setUpdateTime(LocalDateTime.now());
        userInfoMapper.updateById(updateUser);

        authorInfoCacheManager.evictAuthorCache();
        userInfoCacheManager.evictUserCache();
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> deleteBook(Long bookId) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }

        QueryWrapper<BookChapter> chapterQuery = new QueryWrapper<>();
        chapterQuery.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        List<BookChapter> chapters = bookChapterMapper.selectList(chapterQuery);
        List<Long> chapterIds = chapters.stream().map(BookChapter::getId).toList();

        if (!CollectionUtils.isEmpty(chapterIds)) {
            QueryWrapper<BookContent> contentQuery = new QueryWrapper<>();
            contentQuery.in(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterIds);
            bookContentMapper.delete(contentQuery);
            for (Long chapterId : chapterIds) {
                bookChapterCacheManager.evictBookChapterCache(chapterId);
                bookContentCacheManager.evictBookContentCache(chapterId);
            }
        }

        bookChapterMapper.delete(chapterQuery);
        bookInfoMapper.deleteById(bookId);
        bookInfoCacheManager.evictBookInfoCache(bookId);
        amqpMsgManager.sendBookChangeMsg(bookId);
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> deleteBookByName(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return RestResp.fail(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, bookName.trim())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookInfo bookInfo = bookInfoMapper.selectOne(queryWrapper);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        return deleteBook(bookInfo.getId());
    }

    @Override
    public RestResp<PageRespDto<AdminAuditBookItemRespDto>> listPendingBooks(PageReqDto dto) {
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AUDIT_STATUS_PENDING)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        IPage<BookInfo> bookPage = bookInfoMapper.selectPage(page, queryWrapper);
        List<AdminAuditBookItemRespDto> list = bookPage.getRecords().stream()
            .map(v -> AdminAuditBookItemRespDto.builder()
                .bookId(v.getId())
                .bookName(v.getBookName())
                .authorId(v.getAuthorId())
                .authorName(v.getAuthorName())
                .auditStatus(v.getAuditStatus())
                .updateTime(v.getUpdateTime())
                .build())
            .toList();
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<PageRespDto<AdminAuditChapterItemRespDto>> listPendingChapters(PageReqDto dto) {
        IPage<BookChapter> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("audit_status", AUDIT_STATUS_PENDING)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        IPage<BookChapter> chapterPage = bookChapterMapper.selectPage(page, queryWrapper);
        List<Long> bookIds = chapterPage.getRecords().stream().map(BookChapter::getBookId).distinct().toList();
        Map<Long, String> bookNameMap = CollectionUtils.isEmpty(bookIds) ? Map.of() :
            bookInfoMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getId, BookInfo::getBookName, (a, b) -> a));
        List<AdminAuditChapterItemRespDto> list = chapterPage.getRecords().stream()
            .map(v -> AdminAuditChapterItemRespDto.builder()
                .chapterId(v.getId())
                .bookId(v.getBookId())
                .bookName(bookNameMap.get(v.getBookId()))
                .chapterName(v.getChapterName())
                .auditStatus(v.getAuditStatus())
                .updateTime(v.getUpdateTime())
                .build())
            .toList();
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), list));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> auditBook(Long bookId, boolean pass) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        BookInfo updateBook = new BookInfo();
        updateBook.setId(bookId);
        updateBook.setAuditStatus(pass ? AUDIT_STATUS_PASS : AUDIT_STATUS_REJECT);
        updateBook.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(updateBook);
        bookInfoCacheManager.evictBookInfoCache(bookId);
        amqpMsgManager.sendBookChangeMsg(bookId);
        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> auditChapter(Long chapterId, boolean pass) {
        BookChapter chapter = bookChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        BookChapter updateChapter = new BookChapter();
        updateChapter.setId(chapterId);
        updateChapter.setAuditStatus(pass ? AUDIT_STATUS_PASS : AUDIT_STATUS_REJECT);
        updateChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.updateById(updateChapter);
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        return RestResp.ok();
    }

    @Override
    public RestResp<ChapterContentRespDto> getChapterDetail(Long chapterId) {
        BookChapter chapter = bookChapterMapper.selectById(chapterId);
        if (chapter == null) {
            return RestResp.ok(null);
        }
        QueryWrapper<BookContent> contentQuery = new QueryWrapper<>();
        contentQuery.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookContent content = bookContentMapper.selectOne(contentQuery);
        return RestResp.ok(ChapterContentRespDto.builder()
            .chapterName(chapter.getChapterName())
            .chapterContent(content == null ? "" : content.getContent())
            .isVip(chapter.getIsVip())
            .build());
    }

    @Override
    public RestResp<PageRespDto<AdminCommentItemRespDto>> listAllComments(PageReqDto dto) {
        IPage<BookComment> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookComment> commentPage = bookCommentMapper.selectPage(page, queryWrapper);

        List<Long> bookIds = commentPage.getRecords().stream().map(BookComment::getBookId).distinct().toList();
        Map<Long, String> bookNameMap = CollectionUtils.isEmpty(bookIds) ? Map.of() :
            bookInfoMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(BookInfo::getId, BookInfo::getBookName, (a, b) -> a));

        List<Long> userIds = commentPage.getRecords().stream().map(BookComment::getUserId).distinct().toList();
        Map<Long, String> userNameMap = CollectionUtils.isEmpty(userIds) ? Map.of() :
            userInfoMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, UserInfo::getUsername, (a, b) -> a));

        List<AdminCommentItemRespDto> list = commentPage.getRecords().stream()
            .map(v -> AdminCommentItemRespDto.builder()
                .commentId(v.getId())
                .bookId(v.getBookId())
                .bookName(bookNameMap.get(v.getBookId()))
                .userId(v.getUserId())
                .username(userNameMap.get(v.getUserId()))
                .commentContent(v.getCommentContent())
                .auditStatus(v.getAuditStatus())
                .createTime(v.getCreateTime())
                .build())
            .toList();

        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), list));
    }

    @Override
    public RestResp<Void> deleteComment(Long commentId) {
        bookCommentMapper.deleteById(commentId);
        return RestResp.ok();
    }

    /**
     * 根据用户ID查询作者记录。
     */
    private AuthorInfo getAuthorByUserId(Long userId) {
        QueryWrapper<AuthorInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.AuthorInfoTable.COLUMN_USER_ID, userId)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return authorInfoMapper.selectOne(queryWrapper);
    }
}
