多端并存：客户端登录时携带或生成一个设备 id（例如 header X-Device-Id 或客户端本地生成并保存），服务端按 user:session:{userId}:{deviceId} 保存会话信息（TokenPair + 元数据），并额外用 token:jti:{jti} 映射到该 sessionKey，便于按 token 立即校验/注销。
判断设备：优先使用客户端提供的设备 id；同时可以通过 User-Agent 推断设备类型（PC / MOBILE / OTHER）。
提供常用操作：创建会话（登录）、校验 access token、按设备登出