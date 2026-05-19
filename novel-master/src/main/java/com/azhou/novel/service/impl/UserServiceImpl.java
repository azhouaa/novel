package com.azhou.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.azhou.novel.dao.entity.AuthorInfo;
import com.azhou.novel.dao.entity.BookChapter;
import com.azhou.novel.dao.entity.BookInfo;
import com.azhou.novel.dao.entity.UserBookshelf;
import com.azhou.novel.dao.entity.UserFeedback;
import com.azhou.novel.dao.entity.UserInfo;
import com.azhou.novel.dao.mapper.AuthorInfoMapper;
import com.azhou.novel.dao.mapper.BookChapterMapper;
import com.azhou.novel.dao.mapper.BookInfoMapper;
import com.azhou.novel.dao.mapper.UserBookshelfMapper;
import com.azhou.novel.dao.mapper.UserFeedbackMapper;
import com.azhou.novel.dao.mapper.UserInfoMapper;
import com.azhou.novel.core.common.constant.CommonConsts;
import com.azhou.novel.core.common.constant.ErrorCodeEnum;
import com.azhou.novel.core.common.exception.BusinessException;
import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.core.constant.DatabaseConsts;
import com.azhou.novel.core.constant.SystemConfigConsts;
import com.azhou.novel.core.util.JwtUtils;
import com.azhou.novel.dto.req.UserInfoUptReqDto;
import com.azhou.novel.dto.req.UserLoginReqDto;
import com.azhou.novel.dto.req.UserRegisterReqDto;
import com.azhou.novel.dto.resp.UserBookshelfItemRespDto;
import com.azhou.novel.dto.resp.UserInfoRespDto;
import com.azhou.novel.dto.resp.UserLoginRespDto;
import com.azhou.novel.dto.resp.UserRegisterRespDto;
import com.azhou.novel.manager.redis.VerifyCodeManager;
import com.azhou.novel.service.UserService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 会员模块 服务实现类。
 *
 * @author azhou
 * @date 2026/03/10
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;
    private final VerifyCodeManager verifyCodeManager;
    private final UserFeedbackMapper userFeedbackMapper;
    private final UserBookshelfMapper userBookshelfMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookChapterMapper bookChapterMapper;
    private final JwtUtils jwtUtils;
    private final AuthorInfoMapper authorInfoMapper;

    @Override
    public RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto) {
        if (!verifyCodeManager.imgVerifyCodeOk(dto.getSessionId(), dto.getVelCode())) {
            throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
        }
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        if (userInfoMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ErrorCodeEnum.USER_NAME_EXIST);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        userInfo.setUsername(dto.getUsername());
        userInfo.setNickName(dto.getUsername());
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfo.setSalt("0");
        userInfoMapper.insert(userInfo);
        verifyCodeManager.removeImgVerifyCode(dto.getSessionId());
        return RestResp.ok(UserRegisterRespDto.builder()
            .token(jwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
            .uid(userInfo.getId())
            .build());
    }

    @Override
    public RestResp<UserLoginRespDto> login(UserLoginReqDto dto) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (Objects.isNull(userInfo)) {
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        if (!Objects.equals(userInfo.getPassword(), DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }
        return RestResp.ok(UserLoginRespDto.builder()
            .token(jwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
            .uid(userInfo.getId())
            .nickName(userInfo.getNickName()).build());
    }

    @Override
    public RestResp<Void> saveFeedback(Long userId, String content) {
        UserFeedback userFeedback = new UserFeedback();
        userFeedback.setUserId(userId);
        userFeedback.setContent(content);
        userFeedback.setCreateTime(LocalDateTime.now());
        userFeedback.setUpdateTime(LocalDateTime.now());
        userFeedbackMapper.insert(userFeedback);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptReqDto dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setUserSex(dto.getUserSex());
        userInfo.setPreferTags(dto.getPreferTags());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteFeedback(Long userId, Long id) {
        QueryWrapper<UserFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
            .eq(DatabaseConsts.UserFeedBackTable.COLUMN_USER_ID, userId);
        userFeedbackMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getBookshelfStatus(Long userId, Long bookId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        return RestResp.ok(userBookshelfMapper.selectCount(queryWrapper) > 0 ? CommonConsts.YES : CommonConsts.NO);
    }

    @Override
    public RestResp<Void> addToBookshelf(Long userId, Long bookId, Long preContentId) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        UserBookshelf userBookshelf = getBookshelfRecord(userId, bookId);
        LocalDateTime now = LocalDateTime.now();
        if (userBookshelf == null) {
            UserBookshelf newBookshelf = new UserBookshelf();
            newBookshelf.setUserId(userId);
            newBookshelf.setBookId(bookId);
            newBookshelf.setPreContentId(preContentId != null ? preContentId : findFirstChapterId(bookId));
            newBookshelf.setCreateTime(now);
            newBookshelf.setUpdateTime(now);
            userBookshelfMapper.insert(newBookshelf);
            return RestResp.ok();
        }
        if (preContentId != null) {
            userBookshelf.setPreContentId(preContentId);
        }
        userBookshelf.setUpdateTime(now);
        userBookshelfMapper.updateById(userBookshelf);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> removeFromBookshelf(Long userId, Long bookId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        userBookshelfMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<List<UserBookshelfItemRespDto>> listBookshelf(Long userId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .orderByDesc(DatabaseConsts.CommonColumnEnum.UPDATE_TIME.getName());
        List<UserBookshelf> shelfList = userBookshelfMapper.selectList(queryWrapper);
        if (shelfList.isEmpty()) {
            return RestResp.ok(List.of());
        }
        List<Long> bookIds = shelfList.stream().map(UserBookshelf::getBookId).toList();
        QueryWrapper<BookInfo> bookInfoQueryWrapper = new QueryWrapper<>();
        bookInfoQueryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIds);
        Map<Long, BookInfo> bookInfoMap = bookInfoMapper.selectList(bookInfoQueryWrapper).stream()
            .collect(Collectors.toMap(BookInfo::getId, v -> v));
        Set<Long> chapterIds = shelfList.stream().map(UserBookshelf::getPreContentId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BookChapter> chapterMap = Map.of();
        if (!chapterIds.isEmpty()) {
            QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
            chapterQueryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), chapterIds);
            chapterMap = bookChapterMapper.selectList(chapterQueryWrapper).stream()
                .collect(Collectors.toMap(BookChapter::getId, v -> v));
        }
        List<UserBookshelfItemRespDto> respList = new ArrayList<>();
        for (UserBookshelf shelf : shelfList) {
            BookInfo bookInfo = bookInfoMap.get(shelf.getBookId());
            if (bookInfo == null) {
                continue;
            }
            BookChapter chapter = chapterMap.get(shelf.getPreContentId());
            Long continueChapterId = shelf.getPreContentId() != null ? shelf.getPreContentId() : findFirstChapterId(bookInfo.getId());
            String chapterName = chapter != null ? chapter.getChapterName() : "从第一章开始";
            respList.add(UserBookshelfItemRespDto.builder()
                .bookId(bookInfo.getId())
                .bookName(bookInfo.getBookName())
                .picUrl(bookInfo.getPicUrl())
                .authorName(bookInfo.getAuthorName())
                .continueChapterId(continueChapterId)
                .lastReadChapterId(shelf.getPreContentId())
                .lastReadChapterName(chapterName)
                .lastChapterName(bookInfo.getLastChapterName())
                .updateTime(shelf.getUpdateTime())
                .build());
        }
        return RestResp.ok(respList);
    }

    @Override
    public RestResp<Long> getBookshelfReadChapterId(Long userId, Long bookId) {
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new BusinessException(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        UserBookshelf userBookshelf = getBookshelfRecord(userId, bookId);
        if (userBookshelf == null || userBookshelf.getPreContentId() == null) {
            return RestResp.ok(findFirstChapterId(bookInfo.getId()));
        }
        return RestResp.ok(userBookshelf.getPreContentId());
    }

    @Override
    public RestResp<Void> updateBookshelfReadProgress(Long userId, Long bookId, Long chapterId) {
        if (userId == null || bookId == null || chapterId == null) {
            return RestResp.ok();
        }
        UserBookshelf userBookshelf = getBookshelfRecord(userId, bookId);
        if (userBookshelf == null) {
            return RestResp.ok();
        }
        if (Objects.equals(userBookshelf.getPreContentId(), chapterId)) {
            return RestResp.ok();
        }
        userBookshelf.setPreContentId(chapterId);
        userBookshelf.setUpdateTime(LocalDateTime.now());
        userBookshelfMapper.updateById(userBookshelf);
        return RestResp.ok();
    }

    @Override
    public RestResp<UserInfoRespDto> getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        QueryWrapper<AuthorInfo> authorQueryWrapper = new QueryWrapper<>();
        authorQueryWrapper.eq(DatabaseConsts.AuthorInfoTable.COLUMN_USER_ID, userId)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        AuthorInfo authorInfo = authorInfoMapper.selectOne(authorQueryWrapper);
        Integer isAuthor = authorInfo == null ? 0 : 1;
        Integer authorStatus = authorInfo == null ? null : authorInfo.getStatus();
        Integer canUploadNovel = Objects.equals(userInfo.getCanUploadNovel(), 1) ? 1 : 0;
        Integer isAdmin = Objects.equals(userInfo.getIsAdmin(), 1) ? 1 : 0;
        return RestResp.ok(UserInfoRespDto.builder()
            .username(userInfo.getUsername())
            .nickName(userInfo.getNickName())
            .userSex(userInfo.getUserSex())
            .userPhoto(userInfo.getUserPhoto())
            .preferTags(userInfo.getPreferTags())
            .isAuthor(isAuthor)
            .authorStatus(authorStatus)
            .canUploadNovel(canUploadNovel)
            .isAdmin(isAdmin)
            .penName(authorInfo == null ? null : authorInfo.getPenName())
            .build());
    }

    private UserBookshelf getBookshelfRecord(Long userId, Long bookId) {
        if (userId == null || bookId == null) {
            return null;
        }
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return userBookshelfMapper.selectOne(queryWrapper);
    }

    private Long findFirstChapterId(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter firstChapter = bookChapterMapper.selectOne(queryWrapper);
        return firstChapter == null ? null : firstChapter.getId();
    }
}


