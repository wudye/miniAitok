import Cookies from 'js-cookie'

const TokenKey = 'accesstoken'

export function hasToken() {
    return getToken() != null || getToken() != undefined || getToken() != ''
}

export function getToken() {
    return Cookies.get(TokenKey)
}

export function setToken(token: string) {
    return Cookies.set(TokenKey, token, {expires: 3})  // 3天过期
}

export function removeToken() {
    return Cookies.remove(TokenKey)
}
