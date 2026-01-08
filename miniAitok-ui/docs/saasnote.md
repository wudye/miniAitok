Sass / SCSS： Sass 是一种 CSS 预处理器（preprocessor），用于在写样式时引入变量、嵌套、混入（mixin）、函数、导入等编程式功能，最终编译为普通的 CSS。SCSS 是 Sass 的一种语法形式（更常用），文件扩展名为 .scss，语法完全兼容普通 CSS（有大括号和分号）。另一种较老的缩进语法使用 .sass 扩展名（不常用）。
语法差异（简短）

.scss：看起来像普通 CSS，但支持变量、嵌套、混入等扩展。
.sass：基于缩进，不使用大括号和分号，语法更像 Python 风格。
建议使用 .scss，因为与现有 CSS 兼容性最好，学习成本低。
主要优点

变量（$color）便于管理主题色、间距等。
嵌套（selector nesting）让结构更清晰，减少重复选择器。
Mixins、函数：复用样式片段或封装跨浏览器代码。
模块化：可以通过 partials（以 _ 开头的文件）和 @use/@import 拆分样式。
控制指令：@if、@for、@each 支持逻辑与循环，便于生成重复样式（例如图表、网格类）。
简短示例

变量与嵌套：
$primary: #1e90ff;

.header {
  background: $primary;
  .title {
    color: white;
    &:hover { color: lighten($primary, 10%); }
  }
}

Mixin：
@mixin ellipsis($lines: 1) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: $lines;
  overflow: hidden;
}
.card-title { @include ellipsis(2); }