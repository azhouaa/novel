import request from '../utils/request'

/**
 * 获取管理后台大屏统计。
 */
export function getAdminDashboard() {
    return request.get('/admin/dashboard');
}

/**
 * 获取作家专区大屏统计。
 */
export function getAuthorDashboard() {
    return request.get('/author/dashboard');
}
