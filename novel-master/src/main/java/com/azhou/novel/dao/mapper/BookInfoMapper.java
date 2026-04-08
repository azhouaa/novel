package com.azhou.novel.dao.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.azhou.novel.core.annotation.ValidateSortOrder;
import com.azhou.novel.dao.entity.BookInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.azhou.novel.dto.req.BookSearchReqDto;
import com.azhou.novel.dto.resp.BookInfoRespDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 小说信息 Mapper 接口
 * </p>
 *
 * @author xiongxiaoyang
 * @date 2022/05/11
 */
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 增加小说点击量
     *
     * @param bookId 小说ID
     */
    void addVisitCount(@Param("bookId") Long bookId);

    /**
     * 小说搜索
     * @param page mybatis-plus 分页对象
     * @param condition 搜索条件
     * @return 返回结果
     * */
    List<BookInfo> searchBooks(IPage<BookInfoRespDto> page, @ValidateSortOrder BookSearchReqDto condition);

    /**
     * 按分类名查询最新小说
     *
     * @param categoryNames 分类名列表
     * @param limit 数量
     * @return 小说列表
     */
    List<BookInfo> listBooksByCategoryNames(@Param("categoryNames") List<String> categoryNames,
        @Param("limit") Integer limit);

    /**
     * 随机查询小说
     *
     * @param limit 数量
     * @return 小说列表
     */
    List<BookInfo> listRandomBooks(@Param("limit") Integer limit);

}
