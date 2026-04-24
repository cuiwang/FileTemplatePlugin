package com.cuiwang.vuetemplateplugin.ui

import com.cuiwang.vuetemplateplugin.model.VueTemplateSettings
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableColumn
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.table.DefaultTableCellRenderer
import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.JBColor
import java.io.File
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class VueTemplateConfigurable : Configurable {
    private val LOG = Logger.getInstance(VueTemplateConfigurable::class.java)
    private var mainPanel: JPanel? = null
    private val columnNames = arrayOf("#", "模板名称", "文件后缀", "模板类型", "模板内容", "是否启用")
    private val tableModel = object : DefaultTableModel(columnNames, 0) {
        override fun getColumnClass(column: Int): Class<*> {
            return when (column) {
                5 -> java.lang.Boolean::class.java
                else -> String::class.java
            }
        }

        override fun isCellEditable(row: Int, column: Int): Boolean {
            // 序号列（0）不可编辑，其它列可编辑
            return column != 0
        }
    }
    private val table = JBTable(tableModel)

    init {
        // Increase font size for table and editors
        table.font = table.font.deriveFont(table.font.size.toFloat() + 2f)
        table.setRowHeight(30)
        // 优化列宽：序号 40，模板名称 80，文件后缀 80，模板类型 80，模板内容 200，是否启用 60
        table.columnModel.getColumn(0).preferredWidth = 40  // 序号
        table.columnModel.getColumn(1).preferredWidth = 80  // 模板名称
        table.columnModel.getColumn(2).preferredWidth = 80  // 文件后缀
        table.columnModel.getColumn(3).preferredWidth = 80  // 模板类型
        table.columnModel.getColumn(4).preferredWidth = 200 // 模板内容（多行）
        table.columnModel.getColumn(5).preferredWidth = 60  // 是否启用
        table.fillsViewportHeight = true
        table.autoCreateRowSorter = true
        // 保持合适的自动调整行为，让列宽更贴合首选宽度
        table.autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS

        // set cell editors with placeholders
        setCellEditors()
    }

    private fun setCellEditors() {
        // 文本字段编辑器（含编辑时占位符模拟）
        val textEditor = object : AbstractCellEditor(), TableCellEditor {
            private val tf = JBTextField()
            init { tf.font = table.font }
            private var placeholder = ""
            private val placeholderColor = JBColor.GRAY
            private val normalColor = tf.foreground

            override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): java.awt.Component {
                val v = value as? String ?: ""
                placeholder = when (column) {
                    1 -> "请输入模板名称"
                    2 -> "例如: .vue 或 vue"
                    3 -> "请输入模板类型"
                    else -> ""
                }
                tf.toolTipText = placeholder

                if (v.isEmpty()) {
                    tf.text = placeholder
                    tf.foreground = placeholderColor
                    // on focus gained clear placeholder
                    tf.addFocusListener(object : FocusAdapter() {
                        override fun focusGained(e: FocusEvent?) {
                            if (tf.text == placeholder) {
                                tf.text = ""
                                tf.foreground = normalColor
                            }
                        }

                        override fun focusLost(e: FocusEvent?) {
                            if (tf.text.isEmpty()) {
                                tf.text = placeholder
                                tf.foreground = placeholderColor
                            }
                            stopCellEditing()
                            apply()
                        }
                    })
                } else {
                    tf.text = v
                    tf.foreground = normalColor
                    // ensure focusLost still triggers apply
                    tf.addFocusListener(object : FocusAdapter() {
                        override fun focusLost(e: FocusEvent?) {
                            stopCellEditing()
                            apply()
                        }
                    })
                }

                // save on Enter
                tf.addActionListener {
                    stopCellEditing()
                    apply()
                }
                return tf
            }

            override fun getCellEditorValue(): Any {
                val text = tf.text
                return if (text == placeholder) "" else text
            }
        }

        // 多行编辑器（用于模板内容列），使用 JTextArea，保留换行符
        val multiLineEditor = object : AbstractCellEditor(), TableCellEditor {
            private val ta = JTextArea()
            init { ta.font = table.font }
            private val scroll = JBScrollPane(ta)

            init {
                ta.lineWrap = true
                ta.wrapStyleWord = true
                // 缩小多行编辑器的宽度为 200，避免占用过多横向空间
                scroll.preferredSize = Dimension(200, 120)
            }

            override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): java.awt.Component {
                val v = value as? String ?: ""
                if (v.isEmpty()) {
                    ta.text = "请输入模板内容（支持多行）"
                    ta.foreground = JBColor.GRAY
                    ta.addFocusListener(object : FocusAdapter() {
                        override fun focusGained(e: FocusEvent?) {
                            if (ta.text == "请输入模板内容（支持多行）") {
                                ta.text = ""
                                ta.foreground = JBColor.BLACK
                            }
                        }

                        override fun focusLost(e: FocusEvent?) {
                            if (ta.text.isEmpty()) {
                                ta.text = "请输入模板内容（支持多行）"
                                ta.foreground = JBColor.GRAY
                            }
                            stopCellEditing()
                            apply()
                        }
                    })
                } else {
                    ta.text = v
                    ta.foreground = JBColor.BLACK
                    ta.addFocusListener(object : FocusAdapter() {
                        override fun focusLost(e: FocusEvent?) {
                            stopCellEditing()
                            apply()
                        }
                    })
                }
                return scroll
            }

            override fun getCellEditorValue(): Any {
                return ta.text
            }
        }

        // 多行渲染器，显示带换行的文本
        val multiLineRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): java.awt.Component {
                val ta = JTextArea()
                val textVal = (value as? String)
                if (textVal.isNullOrEmpty()) {
                    ta.text = "请输入模板内容（支持多行）"
                    ta.foreground = JBColor.GRAY
                } else {
                    ta.text = textVal
                }
                ta.lineWrap = true
                ta.wrapStyleWord = true
                ta.isOpaque = true
                ta.background = if (isSelected && table != null) table.selectionBackground else UIManager.getColor("Table.background")
                ta.font = table?.font ?: ta.font
                // optional: set a preferred size to help visibility
                ta.border = UIManager.getBorder("Table.cellBorder")
                return ta
            }
        }

        // type column as text input (user can type new types)
        setEditorForColumn(1, textEditor)
        setEditorForColumn(2, textEditor)
        setEditorForColumn(3, textEditor)
        // column 4 = template content uses multi-line editor and renderer
        setEditorForColumn(4, multiLineEditor)
        table.columnModel.getColumn(4).cellRenderer = multiLineRenderer

        // 为文本列（1..3）设置一个占位符渲染器，使得当单元格为空且未处于编辑时显示占位提示
        val placeholders = mapOf(
            1 to "请输入模板名称",
            2 to "例如: .vue 或 vue",
            3 to "请输入模板类型",
            4 to "请输入模板内容（支持多行）"
        )
        val placeholderRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): java.awt.Component {
                val comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                if (comp is JLabel) {
                    val text = (value as? String)?.takeIf { it.isNotEmpty() }
                    if (text == null) {
                        comp.text = placeholders[column] ?: ""
                        if (!isSelected) comp.foreground = JBColor.GRAY
                    } else {
                        comp.text = text
                    }
                }
                return comp
            }
        }
        // 只为单行文本列（1..3）设置占位符渲染器，避免覆盖多行渲染器（col 4）
        for (col in 1..3) {
            table.columnModel.getColumn(col).cellRenderer = placeholderRenderer
        }
    }

    // 更新第一列序号，使其从1开始并保持与行号一致
    private fun updateSequenceNumbers() {
        for (i in 0 until tableModel.rowCount) {
            tableModel.setValueAt((i + 1).toString(), i, 0)
        }
    }

    private fun setEditorForColumn(col: Int, editor: TableCellEditor) {
        val tc: TableColumn = table.columnModel.getColumn(col)
        tc.cellEditor = editor
    }

    override fun createComponent(): JComponent? {
        if (mainPanel == null) {
            try {
                mainPanel = JPanel(BorderLayout())

                val top = JPanel(BorderLayout())
                val hint = JLabel("在此配置模版。")
                val url = "https://github.com/cuiwang/FileTemplatePlugin"
                val link = LinkLabel<String>("查看详细信息", null)
                link.setListener({ _, _ ->
                    try {
                        BrowserUtil.browse(url)
                    } catch (ex: Exception) {
                        LOG.error("打开链接失败: $url", ex)
                    }
                }, null)
                link.setToolTipText(url)
                // left side: hint text + link
                val hintPanel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
                val leftBtns = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
                val importBtn = JButton("导入")
                val exportBtn = JButton("导出")
                leftBtns.add(importBtn)
                leftBtns.add(exportBtn)
                hintPanel.add(hint)
                hintPanel.add(link)
                hintPanel.add(leftBtns)
                top.add(hintPanel, BorderLayout.NORTH)

                // ops panel (four table operation buttons) left-aligned
                val ops = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT))
                val addBtn = JButton("新增")
                val deleteBtn = JButton("删除")
                val upBtn = JButton("上移")
                val downBtn = JButton("下移")
                ops.add(addBtn)
                ops.add(deleteBtn)
                ops.add(upBtn)
                ops.add(downBtn)
                top.add(ops, BorderLayout.SOUTH)

                mainPanel!!.add(top, BorderLayout.NORTH)

                val scroll = JBScrollPane(table)
                scroll.preferredSize = Dimension(900, 360)
                mainPanel!!.add(scroll, BorderLayout.CENTER)

                // 导出
                exportBtn.addActionListener {
                    try {
                        val exported = exportToJson()
                        if (exported) {
                            JOptionPane.showMessageDialog(mainPanel, "导出成功", "导出", JOptionPane.INFORMATION_MESSAGE)
                        }
                    } catch (ex: Exception) {
                        LOG.error("Export failed", ex)
                        JOptionPane.showMessageDialog(mainPanel, "导出失败: ${sanitize(ex.message)}", "错误", JOptionPane.ERROR_MESSAGE)
                    }
                }

                // 导入（追加） — 不再显示导入成功弹窗，仅在失败时提示错误
                importBtn.addActionListener {
                    try {
                        val count = importFromJson()
                        if (count > 0) {
                            reset()
                            updateSequenceNumbers()
                            // intentionally no success dialog to avoid noisy prompts
                        }
                        // if count == 0 -> user canceled or no items; do nothing
                    } catch (ex: Exception) {
                        LOG.error("Import failed", ex)
                        JOptionPane.showMessageDialog(mainPanel, "导入失败: ${sanitize(ex.message)}", "错误", JOptionPane.ERROR_MESSAGE)
                    }
                }

                addBtn.addActionListener {
                    // 新增一行，模板类型不设默认值，留空交由用户选择
                    tableModel.addRow(arrayOf<Any>("", "", "", "", "", java.lang.Boolean.TRUE))
                    updateSequenceNumbers()
                }

                deleteBtn.addActionListener {
                    val sel = table.selectedRow
                    if (sel >= 0) {
                        tableModel.removeRow(sel)
                        updateSequenceNumbers()
                        apply()
                    }
                }

                upBtn.addActionListener {
                    val sel = table.selectedRow
                    if (sel > 0) {
                        val row = tableModel.dataVector.elementAt(sel)
                        val prev = tableModel.dataVector.elementAt(sel - 1)
                        tableModel.dataVector.set(sel - 1, row)
                        tableModel.dataVector.set(sel, prev)
                        tableModel.fireTableDataChanged()
                        updateSequenceNumbers()
                        table.selectionModel.setSelectionInterval(sel - 1, sel - 1)
                        apply()
                    }
                }

                downBtn.addActionListener {
                    val sel = table.selectedRow
                    if (sel >= 0 && sel < tableModel.rowCount - 1) {
                        val row = tableModel.dataVector.elementAt(sel)
                        val next = tableModel.dataVector.elementAt(sel + 1)
                        tableModel.dataVector.set(sel + 1, row)
                        tableModel.dataVector.set(sel, next)
                        tableModel.fireTableDataChanged()
                        updateSequenceNumbers()
                        table.selectionModel.setSelectionInterval(sel + 1, sel + 1)
                        apply()
                    }
                }

                reset()
                updateSequenceNumbers()
            } catch (ex: Exception) {
                LOG.error("Failed to create VueTemplate settings UI", ex)
                mainPanel = JPanel(BorderLayout())
                mainPanel!!.add(JLabel("加载配置页时发生错误: ${ex.message}"), BorderLayout.CENTER)
            }
        }
        return mainPanel
    }

    // Export current templates to JSON file (UTF-8). User chooses file via JFileChooser. Overwrites if exists.
    private val mapper = jacksonObjectMapper()

    private fun exportToJson(): Boolean {
        val chooser = JFileChooser()
        chooser.dialogTitle = "导出模板为 JSON"
        chooser.fileFilter = FileNameExtensionFilter("JSON Files", "json")
        val res = chooser.showSaveDialog(mainPanel)
        if (res != JFileChooser.APPROVE_OPTION) return false
        var file = chooser.selectedFile
        if (!file.name.endsWith(".json", ignoreCase = true)) {
            file = File(file.parentFile, file.name + ".json")
        }
        val templates = VueTemplateSettings.getInstance().state.templates
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, templates)
        return true
    }

    // Import templates from JSON file (UTF-8) and append to existing templates.
    private fun importFromJson(): Int {
        val chooser = JFileChooser()
        chooser.dialogTitle = "从 JSON 导入模板"
        chooser.fileFilter = FileNameExtensionFilter("JSON Files", "json")
        val res = chooser.showOpenDialog(mainPanel)
        if (res != JFileChooser.APPROVE_OPTION) return 0
        val file = chooser.selectedFile
        val imported: List<VueTemplateSettings.Template> = mapper.readValue(file)
        val state = VueTemplateSettings.getInstance().state
        // append imported templates
        for (t in imported) {
            state.templates.add(t)
        }
        return imported.size
    }

    // sanitize messages shown to users: remove control characters that may cause display issues
    private fun sanitize(message: String?): String {
        if (message == null) return "未知错误"
        return message.replace(Regex("[\\x00-\\x1F]"), " ").trim()
    }

    override fun isModified(): Boolean {
        val stored = VueTemplateSettings.getInstance().state.templates
        if (stored.size != tableModel.rowCount) return true
        for (i in 0 until tableModel.rowCount) {
            val name = tableModel.getValueAt(i, 1) as? String ?: ""
            val suffix = tableModel.getValueAt(i, 2) as? String ?: ""
            val type = tableModel.getValueAt(i, 3) as? String ?: ""
            val content = tableModel.getValueAt(i, 4) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 5) as? Boolean ?: true
            val t = stored[i]
            if (t.name != name || t.suffix != suffix || t.type != type || t.content != content || t.enabled != enabled) return true
        }
        return false
    }

    override fun apply() {
        val s = VueTemplateSettings.getInstance().state
        s.templates.clear()
        for (i in 0 until tableModel.rowCount) {
            val name = tableModel.getValueAt(i, 1) as? String ?: ""
            val suffix = tableModel.getValueAt(i, 2) as? String ?: ""
            val type = tableModel.getValueAt(i, 3) as? String ?: ""
            val content = tableModel.getValueAt(i, 4) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 5) as? Boolean ?: true
            s.templates.add(VueTemplateSettings.Template(name, suffix, type, content, enabled))
        }
    }

    override fun reset() {
        tableModel.setRowCount(0)
        val stored = VueTemplateSettings.getInstance().state.templates
        for (t in stored) {
            // 第一列为序号占位，后续列为 name, suffix, type, content, enabled
            tableModel.addRow(arrayOf<Any>("", t.name, t.suffix, t.type, t.content, java.lang.Boolean.valueOf(t.enabled)))
        }
    }

    override fun getDisplayName(): String {
        return "File Template"
    }
}
