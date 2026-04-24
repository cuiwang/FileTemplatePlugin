File Template (IntelliJ IDEA 插件)

简短说明

该插件用于在 Project 视图中快速从用户自定义模版创建文件。功能包括：

- 在 Project 视图右键 New 菜单中添加“File Template”新建项；
- 在 Settings -> Other Settings -> File Template 中配置模版（名称、后缀、类型、内容、是否启用）；
- 支持模版的导入/导出（JSON 格式）；
- 创建文件后自动在 IDE 中打开。

安装

- 本仓库可用 Gradle 构建并在 IDEA Sandbox 中运行：

```bash
./gradlew runIde
```

如何使用

1. 打开 Settings -> Other Settings -> File Template 添加或编辑模版；
2. 在 Project 视图中右键文件夹 -> New -> File Template -> 填写文件名并选择模版 -> 创建；
3. 支持导入/导出模版为 JSON 文件以便备份或在不同机器间同步。

开发

- 主要代码位置：
  - UI: src/main/kotlin/com/cuiwang/vuetemplateplugin/ui/
  - Action: src/main/kotlin/com/cuiwang/vuetemplateplugin/action/
  - Settings: src/main/kotlin/com/cuiwang/vuetemplateplugin/model/

- 构建/调试：
  - 使用 Gradle IntelliJ Plugin 构建与运行：

```bash
./gradlew clean patchPluginXml runIde
```

发布到 JetBrains Marketplace

请参阅 PUBLISH.md（仓库根目录），其中包含将插件发布到 plugins.jetbrains.com 的详细步骤与示例命令。

许可

本项目默认使用 MIT 许可证（见 LICENSE）。

