package com.azhou.novel.service.impl;

import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.req.PageReqDto;
import com.azhou.novel.core.common.resp.PageRespDto;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.dao.entity.AuthorInfo;
import com.azhou.novel.dao.entity.BookChapter;
import com.azhou.novel.dao.entity.BookContent;
import com.azhou.novel.dao.entity.BookInfo;
import com.azhou.novel.dao.entity.UserInfo;
import com.azhou.novel.dao.mapper.AuthorInfoMapper;
import com.azhou.novel.dao.mapper.BookChapterMapper;
import com.azhou.novel.dao.mapper.BookContentMapper;
import com.azhou.novel.dao.mapper.BookInfoMapper;
import com.azhou.novel.dao.mapper.UserInfoMapper;
import com.azhou.novel.dto.resp.AdminUserItemRespDto;
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

    private final UserInfoMapper userInfoMapper;

    private final AuthorInfoMapper authorInfoMapper;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

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
