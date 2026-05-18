import request from '../utils/request'

export function register(params) {
    return request.post('/front/user/register', params);
}

export function login(params) {
    return request.post('/front/user/login', params);
}

export function submitFeedBack(params) {
    return request.post('/front/user/feedback', params);
}

export function comment(params) {
    return request.post('/front/user/comment',params);
}

export function deleteComment(id) {
    return request.delete(`/front/user/comment/${id}`);
}

export function updateComment(id,content) {
    return request.putForm(`/front/user/comment/${id}`,content);
}

export function getUserinfo() {
    return request.get('/front/user');
}

export function updateUserInfo(userInfo) {
    return request.put('/front/user',userInfo);
}

export function listComments(params) {
    return request.get('/front/user/comments', { params });
}

export function getBookshelfStatus(bookId) {
    return request.get('/front/user/bookshelf_status', { params: { bookId } });
}

export function addToBookshelf(bookId, preContentId) {
    return request.post(`/front/user/bookshelf/${bookId}`, null, { params: { preContentId } });
}

export function removeFromBookshelf(bookId) {
    return request.delete(`/front/user/bookshelf/${bookId}`);
}

export function listBookshelf() {
    return request.get('/front/user/bookshelf');
}

export function getBookshelfReadChapterId(bookId) {
    return request.get(`/front/user/bookshelf/read_chapter/${bookId}`);
}

/**
 * 导出当前用户书架 Excel。
 */
export function exportBookshelfExcel() {
    return request.get('/front/user/bookshelf/export', {
        responseType: 'blob'
    });
}

/**
 * 管理端：分页查询用户列表。
 */
export function adminListUsers(params) {
    return request.get('/admin/users', { params });
}

/**
 * 管理端：同意作家申请。
 */
export function adminApproveAuthor(userId) {
    return request.post(`/admin/author/approve/${userId}`);
}

/**
 * 管理端：撤销作家权限。
 */
export function adminRevokeAuthor(userId) {
    return request.post(`/admin/author/revoke/${userId}`);
}

/**
 * 管理端：封禁账号。
 */
export function adminBanUser(userId) {
    return request.post(`/admin/user/ban/${userId}`);
}

/**
 * 管理端：解封账号。
 */
export function adminUnbanUser(userId) {
    return request.post(`/admin/user/unban/${userId}`);
}

/**
 * 管理端：删除小说。
 */
export function adminDeleteBook(bookId) {
    return request.post(`/admin/book/delete/${bookId}`);
}

/**
 * 管理端：按书名删除小说（演示版）。
 */
export function adminDeleteBookByName(bookName) {
    return request.post('/admin/book/delete_by_name', null, { params: { bookName } });
}

/**
 * 管理端：分页查询待审核书籍。
 */
export function adminListPendingBooks(params) {
    return request.get('/admin/audit/books/pending', { params });
}

/**
 * 管理端：分页查询待审核章节。
 */
export function adminListPendingChapters(params) {
    return request.get('/admin/audit/chapters/pending', { params });
}

/**
 * 管理端：审核书籍。
 */
export function adminAuditBook(bookId, pass) {
    return request.post(`/admin/audit/book/${bookId}`, null, { params: { pass } });
}

/**
 * 管理端：审核章节。
 */
export function adminAuditChapter(chapterId, pass) {
    return request.post(`/admin/audit/chapter/${chapterId}`, null, { params: { pass } });
}

/**
 * 管理端：查看待审核章节详情。
 */
export function adminGetChapterDetail(chapterId) {
    return request.get(`/admin/audit/chapter/${chapterId}`);
}
