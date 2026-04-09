package com.azhou.novel.service;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.req.UserInfoUptReqDto;
import com.azhou.novel.dto.req.UserLoginReqDto;
import com.azhou.novel.dto.req.UserRegisterReqDto;
import com.azhou.novel.dto.resp.UserBookshelfItemRespDto;
import com.azhou.novel.dto.resp.UserInfoRespDto;
import com.azhou.novel.dto.resp.UserLoginRespDto;
import com.azhou.novel.dto.resp.UserRegisterRespDto;
import java.util.List;

/**
 * 会员模块 服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return JWT
     */
    RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return JWT + 昵称
     */
    RestResp<UserLoginRespDto> login(UserLoginReqDto dto);

    /**
     * 用户反馈
     *
     * @param userId  反馈用户ID
     * @param content 反馈内容
     * @return void
     */
    RestResp<Void> saveFeedback(Long userId, String content);

    /**
     * 用户信息修改
     *
     * @param dto 用户信息
     * @return void
     */
    RestResp<Void> updateUserInfo(UserInfoUptReqDto dto);

    /**
     * 用户反馈删除
     *
     * @param userId 用户ID
     * @param id     反馈ID
     * @return void
     */
    RestResp<Void> deleteFeedback(Long userId, Long id);

    /**
     * 查询书架状态接口
     *
     * @param userId 用户ID
     * @param bookId 小说ID
     * @return 0-不在书架 1-已在书架
     */
    RestResp<Integer> getBookshelfStatus(Long userId, Long bookId);

    /**
     * 加入书架
     *
     * @param userId       用户ID
     * @param bookId       小说ID
     * @param preContentId 阅读进度章节ID
     * @return void
     */
    RestResp<Void> addToBookshelf(Long userId, Long bookId, Long preContentId);

    /**
     * 移出书架
     *
     * @param userId 用户ID
     * @param bookId 小说ID
     * @return void
     */
    RestResp<Void> removeFromBookshelf(Long userId, Long bookId);

    /**
     * 查询书架列表
     *
     * @param userId 用户ID
     * @return 书架列表
     */
    RestResp<List<UserBookshelfItemRespDto>> listBookshelf(Long userId);

    /**
     * 查询某本书的续读章节ID
     *
     * @param userId 用户ID
     * @param bookId 小说ID
     * @return 续读章节ID
     */
    RestResp<Long> getBookshelfReadChapterId(Long userId, Long bookId);

    /**
     * 更新书架阅读进度（仅在已加入书架时更新）
     *
     * @param userId    用户ID
     * @param bookId    小说ID
     * @param chapterId 当前阅读章节ID
     * @return void
     */
    RestResp<Void> updateBookshelfReadProgress(Long userId, Long bookId, Long chapterId);

    /**
     * 用户信息查询
     * @param userId 用户ID
     * @return 用户信息
     */
    RestResp<UserInfoRespDto> getUserInfo(Long userId);
}
