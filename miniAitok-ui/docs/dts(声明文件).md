d.ts（声明文件）：TypeScript 的「类型声明」文件，用来告诉编译器和编辑器某些 JavaScript/第三方模块或全局变量有哪些类型/导出。声明文件本身只有类型信息，不包含运行时代码。
为什么需要

当你从无类型的 JS 文件或第三方包导入时，TS 无法推断导出类型，会报 “Could not find a declaration file” 或把模块视为 any。写 .d.ts 可以恢复类型检查、自动补全与错误提示，避免隐式 any 隐藏问题。
常见用途

模块声明：为某个模块（比如 @/utils/roydon）声明命名导出与默认导出。
全局声明：声明全局变量/类型（比如你给 window.$roydon 添加类型）。
库类型打包：当用 TypeScript 写库时，编译器可以生成 .d.ts 供消费者使用。

模块声明示例

// src/types/roydon.d.ts
declare module '@/utils/roydon' {
  export function parseTime(time?: any, pattern?: string): string | null;
  export function removeHtmlTags(html: string): string;
  const roydon: {
    parseTime: typeof parseTime;
    removeHtmlTags: typeof removeHtmlTags;
  };
  export default roydon;
}

全局声明示例



// src/types/global.d.ts
declare global {
  interface Window { $roydon?: typeof import('@/utils/roydon').default }
}
export {};

文件放哪儿 & tsconfig 配置

常放在项目 src/types/ 或 types/ 目录下。
确保 tsconfig.json 能找到它们："typeRoots"（可选）或把 include 指向 src/**/*（常用）。
如果你使用路径别名（@），声明应匹配你实际的 import 字符串（包括是否带 .js）。
如何生成 / 辅助工具

从 TS 源码编译：开启 compilerOptions.declaration: true，构建时会生成 .d.ts。
自动生成工具：dts-gen（第三方）、或者手写并用编辑器类型提示辅助。
对于 JS 文件，可通过 JSDoc + TypeScript 推断生成更准确的声明。
注意事项与最佳实践

优先写准确类型，不要长期使用 any（临时可用）。
区分命名导出与默认导出（export function f vs export default {...}），声明需与运行时一致。
如果代码中有 import '.../module.js'，声明里最好同时声明带 .js 的模块名。
修改 .d.ts 后重启 TS Server（VS Code：TypeScript: Restart TS Server）以立即生效。