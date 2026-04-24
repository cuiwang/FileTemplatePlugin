# PUBLISH.md — 打包与发布到 JetBrains Marketplace 指南

本指南说明如何在本地打包插件并将其上传到 JetBrains Marketplace 的步骤、常见错误与排查方法。

前提

- 已安装 Java 17+ 或根据项目 `build.gradle.kts` 指定的 JDK（本项目使用 JDK 21 作为目标兼容版本）。
- 已安装 Gradle Wrapper（项目根目录自带 `gradlew`）。
- 已在 https://plugins.jetbrains.com/ 上创建开发者账号并获取 API Token（用于上传，如果你需要自动化上传，可使用 Gradle 的 `publishPlugin` 任务并设置 `intellij.publishToken`）。

本地打包

在项目根目录运行：

```bash
./gradlew clean build
```

构建成功后，插件发行文件会出现在 `build/distributions/` 目录下，例如 `VueTemplate-Plugin-1.1.zip`。

在 IDE 中测试

1. 使用 Gradle 任务 `runIde` 或在 IntelliJ IDEA/ WebStorm 中使用 Run Configuration "Run IDE with Plugin" 启动调试沙箱：

```bash
./gradlew runIde
```

2. 在启动的沙箱 IDE 中，打开 Settings -> Other Settings -> File Template，检查配置页是否显示并能正常增删改。
3. 在项目视图中右键目标目录，选择 New -> File Template 来创建文件，检查文件是否按模板内容创建并自动打开。

准备上传到 Marketplace

1. 确保 `plugin.xml` 中的 `<id>` 为全小写且不包含单词 "plugin"，以避免 Marketplace 的校验错误（例如：`com.cuiwang.filetemplate`）。
2. 确保 `build.gradle.kts` 中的 `version` 已设置为你要发布的版本（例如 `1.1`）。
3. 在 `build.gradle.kts` 中配置 `intellijPublish` 或在命令行使用 `publishPlugin`（需要在 `~/.gradle/gradle.properties` 中配置 `intellijPublishToken=<your-token>`）：

```bash
# 使用 Gradle 发布（需提前配置 token）
./gradlew publishPlugin
```

手动上传

1. 到 https://plugins.jetbrains.com/ ，登录开发者账户。
2. 在 "My Plugins" 页面选择或创建插件条目，上传 `build/distributions/<your-plugin>.zip`。
3. 填写插件页面信息（使用 `plugin-page.md` 中的内容作为参考），上传截图、选择兼容平台并提交审核。

常见错误与排查

- Invalid plugin descriptor 'plugin.xml': The plugin name 'File Template-Plugin' should not include the word 'plugin'.
  - 解决：检查 `plugin.xml` 的 `<name>` 字段，确保不包含 "plugin" 文本；同时确保 `<id>` 符合要求（小写、点分名）。
- JSON 导入/导出错误导致上传失败或运行时抛异常。
  - 解决：使用 Jackson 序列化/反序列化，并在导出时使用 `writerWithDefaultPrettyPrinter()` 来生成合法 JSON；导入时捕获异常并向用户提示。
- 运行时界面卡死或“Loading...”问题。
  - 解决：检查 `applicationConfigurable` 的实现是否在 EDT 中进行 UI 操作。尽量遵循 ActionUpdateThread 的约束，避免在 BGT 中访问 EDT-only 数据。

发布后的建议

- 在插件首页提供 2-3 张清晰截图（Settings 页面、创建对话框、示例生成的文件），并在发布说明中列出 1-2 个常见使用场景。
- 在 README.md 中详细记录模版语法与导入/导出格式样例。

如果你希望，我可以：
- 代你生成插件市场页面的完整 Markdown（更长的描述、常见问题、支持链接）；
- 代你运行 `./gradlew publishPlugin`（需要你提供或在当前环境设置 `intellijPublishToken`）。

