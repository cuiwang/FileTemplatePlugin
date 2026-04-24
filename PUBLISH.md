发布到 JetBrains Marketplace（简要指南）

前提：
- 已在 https://plugins.jetbrains.com/ 创建开发者账户并生成 Token。
- 本地环境已安装 JDK 与 Gradle。

步骤：

1. 在 plugins.jetbrains.com 上创建插件条目并记录 Plugin ID（通常与 `plugin.xml` 中的 `<id>` 保持一致）。

2. 在项目中添加 IntelliJ Plugin Publish 配置（本项目使用 `org.jetbrains.intellij.platform` Gradle 插件）。

3. 使用以下命令构建并发布（需要在环境变量中设置 `PUBLISH_TOKEN`）：

```bash
# 构建 plugin
./gradlew clean buildPlugin

# 发布到 JetBrains Marketplace（使用 gradle-intellij-plugin 的 publish 任务）
# PUBLISH_TOKEN: 你的 jetsbrains 插件 token
./gradlew publishPlugin -PpublishToken=$PUBLISH_TOKEN
```

4. 发布后在 plugins.jetbrains.com 的插件页面完成描述、分类、图标等信息的填写，并提交审核。

常见问题：
- 如果 build 过程中出现缺少依赖或兼容性问题，请检查 `build.gradle.kts` 中的 Idea 版本与 plugin.xml 的依赖。
- token 权限问题：请确保 token 有发布权限。

更多详细说明见官方文档：
https://plugins.jetbrains.com/docs/marketplace/publishing-plugin.html

