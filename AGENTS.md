# Repository Guidelines

## 项目结构与模块组织
本仓库是前后端分离的双模块工作区。
- `novel-master/`：Spring Boot 后端，核心代码在 `src/main/java`，配置与 Mapper 在 `src/main/resources`，测试在 `src/test`。
- `novel-front-web-master/`：Vue 3 前端，主要目录为 `src/api`、`src/components`、`src/views`、`src/assets`。

提交时请按模块拆分变更，避免在一次提交中混合无关后端与前端改动。

## 构建、测试与开发命令
后端（进入 `novel-master/` 后执行）：
- `mvn clean compile`：编译并校验依赖。
- `mvn test`：执行后端测试阶段。
- `mvn spring-boot:run`：本地启动服务（默认配置端口 `8888`）。

前端（进入 `novel-front-web-master/` 后执行）：
- `npm install`：安装依赖。
- `npm run serve`：启动开发服务器。
- `npm run build`：生成生产构建。
- `npm run lint`：执行 ESLint 检查。

## 代码风格与命名规范
Java 使用 4 空格缩进；类名使用 `UpperCamelCase`，方法/字段使用 `lowerCamelCase`，包名保持 `com.azhou.novel.*` 小写结构。建议在 IDEA 中导入 `novel-master/doc/style/intellij-java-google-style.xml`。

Vue 组件文件使用 `PascalCase.vue`（如 `FloatingAiChat.vue`）；接口封装放在 `src/api/*.js`，公共工具放在 `src/utils/`。

项目开发多加中文注释，关键部分一定要加。方法前，类前要加。

## 测试规范
当前自动化测试覆盖有限，新增功能或修复缺陷时应补充测试：
- 后端测试放在 `novel-master/src/test/java`，命名以 `*Test` 结尾。
- 前端新增测试时建议放在 `novel-front-web-master/tests` 或组件旁 `*.spec.js`。

提交 PR 前至少执行：`mvn test` 与 `npm run lint`。

## 提交与 Pull Request 规范
现有历史以简短祈使句提交信息为主（多为中文），例如：`增加图片映射功能`、`重构：包名为com.azhou`。保持一次提交只做一件事。

PR 需包含：变更目的、影响模块、关键配置变更、验证命令与结果；涉及界面改动时附截图或录屏。

## 安全与配置建议
禁止提交真实密钥与口令。`application.yml` 中涉及 API Key、数据库密码、Token 的值应通过环境变量或环境专用配置注入。上线前确认 `spring.profiles.active` 及 Elasticsearch/RabbitMQ/XXL-JOB 开关状态。


## 开发者添加的备注
可以使用数据库，novel novel novel  数据库、账号、密码
技术栈mysql8.0,redis,springboot,springai,vue。
文件夹     /日志/       下有往期ai开发的日志，可以查阅一遍。
读取本地文件主要读项目前后端部分。图片、target、nodemodules、.idea.vocode、.mvn、doc、docker、这些非必要就不要读了，节省token
项目开发多加中文注释，关键部分一定要加

## 技术栈
技术栈：Java 21+, Spring Boot 3.x, Vue 3, Spring AI, redis, mysql8.0