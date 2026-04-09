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
