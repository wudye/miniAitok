在 tsconfig.app.json 中添加了
"baseUrl": "./"
"paths": { "@/*": ["src/*"] }
tsconfig.node.json 已经包含相同映射（无需修改）。
vite.config.ts 中已有 resolve.alias 指向 src，构建器会正确解析 @。