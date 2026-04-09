package com.azhou.novel.service;

import com.azhou.novel.core.common.resp.RestResp;
import com.azhou.novel.dto.req.AuthorRegisterReqDto;

/**
 * 作家模块 业务服务类
 *
 * @author azhou
 * @date 2026/03/10
 */
public interface AuthorService {

    /**
     * 作家注册
     *
     * @param dto 注册参数
     * @return void
     */
    RestResp<Void> register(AuthorRegisterReqDto dto);

    /**
     * 查询作家状态
     *
     * @param userId 用户ID
     * @return 作家状态
     */
    RestResp<Integer> getStatus(Long userId);
}
