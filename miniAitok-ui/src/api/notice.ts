import request from '@/utils/request'

// 分页查询通知
export function noticePage(data : any) {
    return request({
        url: '/notice/api/v1/page',
        method: 'post',
        data: data
    })
}

// 删除通知
export function delNotice(noticeId: string) {
    return request({
        url: '/notice/api/v1/'+noticeId,
        method: 'delete',
    })
}

// 未读通知数量
export function noticeCount(data: any) {
    return request({
        url: '/notice/api/v1/count',
        method: 'post',
        data: data
    })
}
