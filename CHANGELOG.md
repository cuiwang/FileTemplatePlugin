# Changelog

All notable changes to this project will be documented in this file.

## [1.1] - 2026-04-24
### Added
- 在设置页面（Settings -> Other Settings -> File Template）中新增了模板序号列（“#”），从 1 开始显示，便于用户识别模板顺序。
- 在“创建文件”对话框中为模板列表项增加了序号前缀（从 1 开始），并默认选中第一个模板（如果存在）。
- 优化了配置页面的列宽与字体，模板名称/类型列长度调整为 80，模板内容列长度调整为 200。
- 导入/导出功能改进：使用标准 JSON 序列化库（Jackson），导入为追加模式，导出为标准 JSON 文件，修复了特殊字符导致的解析错误。

### Changed
- 修正 `plugin.xml` 中的插件 id（由 `com.cuiwang.FileTemplate-Plugin` 更名为 `com.cuiwang.filetemplate`）以满足 Marketplace 校验。
- 新增设置页面序号更新逻辑，保持序号与列表项一致。
- 将 `NewVueTemplateAction` 的模板列表从字符串变为对象列表，渲染器显示序号和模板名称。

### Fixed
- 修复了多行模板内容编辑器在保存时丢失格式（现在保留原有换行与空白）。
- 修复了导入取消时误报导入成功的问题。
- 修复了部分 AndAction 更新线程使用问题，并规范了 action 的初始化。

### Notes
- 若需序号随表格排序动态变化（即点击列头排序后序号自动更新为可视顺序），可在后续版本实现；当前实现序号代表持久化顺序（配置列表中的顺序）。

