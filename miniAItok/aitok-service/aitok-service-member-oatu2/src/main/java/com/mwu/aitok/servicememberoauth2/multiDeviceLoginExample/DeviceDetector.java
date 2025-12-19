package com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample;


import com.mwu.aitiokcoomon.core.utils.IdUtils;
import com.mwu.aitiokcoomon.core.utils.IpUtils;
import com.mwu.aitiokcoomon.core.utils.ServletUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/*
发送方式
Header：由客户端在每次请求时显式设置（例如 fetch/axios 设置 X-Device-Id）。跨域会触发 CORS 预检，且需要服务器在 Access-Control-Allow-Headers 中允许该自定义头。
Cookie：浏览器根据同源/路径/域规则自动附带发送（除非被 SameSite 或同源策略阻止）。跨域发送需显式开启凭证（fetch 的 credentials: 'include'）。
可见性与控制
Header：前端 JS 可读写，必须在请求时手动添加。
Cookie：可被前端 JS 读写（除非设为 HttpOnly，这样只有服务器可访问）。
安全性
Header：不受浏览器 Cookie 的 CSRF 自动发送机制影响，较不易被 CSRF 利用，但自定义头需要 CORS 配置，能限制来源。
Cookie：若未设置 SameSite/HttpOnly/Secure 可能被 CSRF、XSS 利用。敏感信息通常用 HttpOnly cookie 存储以防前端脚本窃取。
生命周期与持久化
Header：通常由前端在本地（localStorage/sessionStorage）保存并主动发送；生命周期由前端存储决定。
Cookie：可设置过期时间，自动持久化在浏览器，服务器可通过 Set-Cookie 下发并控制作用域与有效期。
跨域/同源行为
Header：跨域需后端允许该头并处理预检；但不会被浏览器自动发送到任意站点。
Cookie：默认仅同源发送，跨域发送需 credentials 并且服务端允许 Access-Control-Allow-Credentials: true，同时 SameSite 可能阻止。
推荐用途（针对 X-Device-Id / DEVICE_ID 场景）
若希望可靠在跨域单页应用中由前端控制并避免自动发送风险，使用自定义 Header 更直观。
若希望由浏览器自动管理、跨请求持久化且由服务器统一下发/撤销，使用 Cookie 更方便，但需注意 HttpOnly/Secure/SameSite 配置和 CSRF 防护。

 */
public final class DeviceDetector {

    /*

    。X-Device-Id 一般由前端（网页或移动客户端）生成并在每次请求时放到自定义请求头里，或者由服务器在之前响应中下发并由客户端保存后续回传。设备本身不会自动发送这个 header，除非客户端代码或原生 App 主动读取某个设备标识并设置进去。
要点（简短）：
前端生成：常用 UUID，保存在 localStorage/sessionStorage 或 Cookie，然后在请求时把它放到 X-Device-Id。
服务器生成：可在首次请求时生成 UUID，返回给客户端并通过 Set-Cookie 或响应体让客户端保存；更安全的做法是对 ID 做签名。
移动端原生 App：可以使用平台提供的设备标识（如 ANDROID_ID、identifierForVendor），但要注意隐私与合规性。
风险：不应把它当作认证凭证（易伪造），必要时在服务端绑定/签名并结合其他信息验证。
     */
    private static final String HEADER_DEVICE_ID = "X-Device-Id";
    private static final String COOKIE_DEVICE_ID = "DEVICE_ID";

    public static DeviceInfo resolve(HttpServletRequest request) {
        String deviceId = null;
        if (request == null) {
            deviceId = IdUtils.fastUUID();
            return new DeviceInfo(deviceId, "OTHER", null, null);
        }

        // 优先 header
        deviceId = request.getHeader(HEADER_DEVICE_ID);

        // 其次 cookie
        if (deviceId == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (COOKIE_DEVICE_ID.equals(c.getName())) {
                        deviceId = c.getValue();
                        break;
                    }
                }
            }
        }

        // 否则生成并返回（客户端应保存）
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = IdUtils.fastUUID();
        }

        String ua = request.getHeader("User-Agent");
        String uaLower = ua == null ? "" : ua.toLowerCase();
        String deviceType = "OTHER";
        if (uaLower.contains("mobile") || uaLower.contains("android") || uaLower.contains("iphone")) {
            deviceType = "MOBILE";
        } else {
            deviceType = "PC";
        }

        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        String ip2 = ServletUtils.getClientIP();
        System.out.println("DeviceDetector.resolve ip=" + ip + ", ip2=" + ip2);
        return new DeviceInfo(deviceId, deviceType, ua, ip);
    }
}
