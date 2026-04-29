# PUBLISH.md — 本地打包与发布到 JetBrains Marketplace 指南

本文档说明如何在本地打包插件、使用沙箱测试，以及准备上传到 JetBrains Marketplace 的步骤与常见排查方法。

前提

- 使用项目自带的 Gradle Wrapper（无需全局安装 Gradle）。
- JDK 17+（项目 target JDK 请参考 build.gradle.kts，如果需要请安装相应 JDK）。
- 已在 https://plugins.jetbrains.com/ 注册开发者账号，并可以获取 API Token（仅在使用自动发布时需要）。

快速本地打包（生成 distributable ZIP）

在项目根目录运行：

```bash
./gradlew clean build
```

构建成功后，插件发行文件位于：

- build/distributions/<plugin-name>-<version>.zip

示例：

- build/distributions/VueTemplate-Plugin-1.4.zip

在 IDE 中本地验证（沙箱运行）

1. 使用 Gradle 任务运行沙箱 IDE：

```bash
./gradlew runIde
```

2. 在启动的沙箱 IDE 中：
   - 打开 Settings -> Other Settings -> File Template，确保配置页面能正常加载并能增删改模板。
   - 在 Project 视图中右键一个文件夹，选择 New -> File Template 测试创建流程，创建后应自动打开新文件。
   - 验证导入/导出功能、图标与界面布局。

准备上传到 JetBrains Marketplace（手动上传）

1. 确认 `plugin.xml` 中的 `<id>` 与 Marketplace 上已存在条目的 ID 完全一致（大小写敏感，Marketplace 不允许更改已发布插件的 ID）。
2. 确认 `build.gradle.kts` 中的 `version` 字段为你要发布的版本号（例如 1.4）。
3. 打开 https://plugins.jetbrains.com/，登录开发者账号，进入 “My Plugins” -> 选择对应插件 -> 上传新的分发包（build/distributions/*.zip）。
4. 填写或更新插件页面内容（可以使用本仓库的 `plugin-page.md` 作为描述），上传 2-3 张清晰截图并选择兼容 IDE 平台。
5. 提交审核并等待 Marketplace 审核结果。

使用 Gradle 自动发布（可选）

将 API token 添加到 ~/.gradle/gradle.properties：

```properties
intellijPublishToken=<your-token>
```

然后运行：

```bash
./gradlew publishPlugin
```

注意：自动发布会将 ZIP 上传到 Marketplace 的当前插件条目，请确保 `plugin.xml` 中的 `<id>` 与 Marketplace 条目匹配。

常见错误与排查

- Invalid plugin descriptor 'plugin.xml': The plugin name 'File Template-Plugin' should not include the word 'plugin'.
  - 原因：Marketplace 对插件名称/描述符有严格校验。解决：在 `plugin.xml` 中的 `<name>` 不应包含 "plugin" 单词；在页面展示时可在 plugin-page.md 中标注完整名字。
- 上传失败提示插件 ID 不匹配（The plugin ID in your upload does not match）：
  - 原因：上传的 ZIP 中 `plugin.xml` 的 `<id>` 与 Marketplace 上条目 ID 不一致。
  - 解决：修改 `src/main/resources/META-INF/plugin.xml` 中的 `<id>` 为 Marketplace 条目的 ID 并重新打包。
- 导出/导入 JSON 导致的异常：
  - 原因：手写 JSON 解析器或不完整转义特殊字符。
  - 解决：使用 Jackson 等标准库进行序列化/反序列化，导出时使用 pretty printer 并在导入时捕获异常提示用户。
- 运行时 Settings 显示 Loading... 且无法渲染：
  - 原因：在初始化或 update 时在非 EDT 线程访问了 EDT-only API；或配置类执行了耗时操作阻塞 UI。
  - 解决：确保 UI 操作在 EDT 中执行，耗时逻辑放到后台任务中，并遵循 ActionUpdateThread 注解约定。

发布后建议

- 在插件页面上传 2-3 张截图：设置页面、创建对话框、生成的示例文件。使用 800x600 以上分辨率截图可提高展示效果。
- 在 README.md 中补充使用示例与 JSON 导入/导出格式样例，方便用户快速上手。
- 如果需要我可以代为生成 Marketplace 页面完整 Markdown（包括常见问题、使用案例和许可信息）并准备发布包。
