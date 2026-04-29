# File Template — 插件市场页面内容

**版本**：1.4  
**发布日期**：2026-04-29

简短说明

File Template 是一款轻量的 JetBrains IDE 插件，帮助你通过可配置的模板快速生成文件，提升组件开发与项目脚手架效率。

详细描述

File Template 为 IDE 添加了可管理的文件模板和一键创建流程，主要特性：

- 在 Settings -> Other Settings -> File Template 中管理模板（字段：序号、名称、后缀、类型、内容、是否启用）。序号从 1 开始，便于识别顺序。
- 模板内容支持多行与原始格式（保留换行与空白），录入时支持占位提示，输入框在失去焦点或按回车后即时保存。
- 新增模板时不再默认设置类型，避免误用；创建对话框若模板列表不为空则默认选中第一个，减少步骤。
- 在 Project 视图右键 -> New -> File Template 快速创建文件，创建后自动在编辑器中打开新文件。
- 右键菜单中的“File Template”项带有合适的 16x16 图标显示（兼容不同主题），并修复了原先图标缩放函数（不再需要 scaleIconToSize）。
- 在设置页面和创建对话框中显示模板序号（从 1 开始）。
- 导入 / 导出（JSON 格式）支持追加模式，并使用标准 JSON 序列化库以正确处理特殊字符；导入/导出按钮位于界面上方提示区末端，操作更清晰。

使用说明

1. 打开 Settings -> Other Settings -> File Template，点击 “新增” 添加模板，填写名称、后缀、类型和多行内容（内容将保留原有格式）。
2. 在项目视图中选择目标目录右键，选择 New -> File Template，选中模板并输入文件名，点击 OK 即可创建并自动打开文件。

截图

- 在这里放置设置页面截图（settings.png）
- 在这里放置创建文件对话框截图（create-dialog.png）
- 在这里放置示例生成文件截图（example-file.png）

标签

file templates, template, scaffolding, productivity

支持

作者：崔旺
项目地址：https://github.com/cuiwang/FileTemplatePlugin

变更记录

参见 CHANGELOG.md
