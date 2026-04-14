package com.azhou.novel.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作者上传记录实体
 *
 * @author azhou
 * @date 2026/04/14
 */
@TableName("author_upload_record")
public class AuthorUploadRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 小说ID
     */
    private Long bookId;

    /**
     * 小说名称
     */
    private String bookName;

    /**
     * 封面地址
     */
    private String picUrl;

    /**
     * TXT文件名
     */
    private String txtFileName;

    /**
     * TXT文件大小
     */
    private Long txtFileSize;

    /**
     * TXT解析编码
     */
    private String txtCharset;

    /**
     * 章节数
     */
    private Integer chapterTotal;

    /**
     * 总字数
     */
    private Integer wordTotal;

    /**
     * 拆分规则
     */
    private String splitRule;

    /**
     * 处理耗时毫秒
     */
    private Long durationMs;

    /**
     * 状态;1-成功 2-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getTxtFileName() {
        return txtFileName;
    }

    public void setTxtFileName(String txtFileName) {
        this.txtFileName = txtFileName;
    }

    public Long getTxtFileSize() {
        return txtFileSize;
    }

    public void setTxtFileSize(Long txtFileSize) {
        this.txtFileSize = txtFileSize;
    }

    public String getTxtCharset() {
        return txtCharset;
    }

    public void setTxtCharset(String txtCharset) {
        this.txtCharset = txtCharset;
    }

    public Integer getChapterTotal() {
        return chapterTotal;
    }

    public void setChapterTotal(Integer chapterTotal) {
        this.chapterTotal = chapterTotal;
    }

    public Integer getWordTotal() {
        return wordTotal;
    }

    public void setWordTotal(Integer wordTotal) {
        this.wordTotal = wordTotal;
    }

    public String getSplitRule() {
        return splitRule;
    }

    public void setSplitRule(String splitRule) {
        this.splitRule = splitRule;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
