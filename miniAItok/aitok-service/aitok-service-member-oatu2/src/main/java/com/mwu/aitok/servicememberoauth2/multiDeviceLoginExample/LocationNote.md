除了基于客户端 IP 的方式外，常见可选方案及优缺点（简短）：
浏览器原生 Geolocation API（高精度，需用户同意；适合经纬度上报）
移动端原生定位（GPS / Wi‑Fi / 基站，精度高，需权限）
用户在注册/个人资料里主动填写地址（最隐私友好，但人为）
第三方认证/社交登录返回的地区信息（精度取决于平台）
接收客户端上报的 Wi‑Fi / 基站信息并调用定位服务（如 Mozilla Location Service）
基于请求的 Accept-Language / 时区 / 时区偏移 推断（粗略，隐私友好）
通过浏览器采集经纬度后在服务端用反向地理编码把经纬度转换为城市/省

前端向 \/api/v1/location`POST{lat,lon}`；后端接收并调用 Nominatim，注意设置 User-Agent、尊重隐私与速率限制


// javascript
// 文件：`src/main/resources/static/js/location.js`
if ('geolocation' in navigator) {
navigator.geolocation.getCurrentPosition(
pos => {
const payload = { lat: pos.coords.latitude, lon: pos.coords.longitude };
fetch('/api/v1/location', {
method: 'POST',
headers: { 'Content-Type': 'application/json' },
body: JSON.stringify(payload)
}).catch(() => {/* 忽略错误 */});
},
err => {
// 用户拒绝或其它错误，按需降级（比如提示用户手动填写）
},
{ enableHighAccuracy: true, timeout: 5000 }
);
}
