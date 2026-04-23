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

class VueTemplateConfigurable : Configurable {
    private val LOG = Logger.getInstance(VueTemplateConfigurable::class.java)
    private var mainPanel: JPanel? = null
    private val columnNames = arrayOf("模板名称", "文件后缀", "模板类型", "模板内容", "是否启用")
    private val tableModel = object : DefaultTableModel(columnNames, 0) {
        override fun getColumnClass(column: Int): Class<*> {
            return when (column) {
                4 -> java.lang.Boolean::class.java
                else -> String::class.java
            }
        }

        override fun isCellEditable(row: Int, column: Int): Boolean {
            return true
        }
    }
    private val table = JBTable(tableModel)

    init {
        table.setRowHeight(28)
        // 优化列宽：模板名称 80，文件后缀 80，模板类型 80，模板内容 200，是否启用 60
        table.columnModel.getColumn(0).preferredWidth = 80  // 模板名称
        table.columnModel.getColumn(1).preferredWidth = 80  // 文件后缀
        table.columnModel.getColumn(2).preferredWidth = 80  // 模板类型
        table.columnModel.getColumn(3).preferredWidth = 200 // 模板内容（多行）
        table.columnModel.getColumn(4).preferredWidth = 60  // 是否启用
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
            private var placeholder = ""
            private val placeholderColor = JBColor.GRAY
            private val normalColor = tf.foreground

            override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): java.awt.Component {
                val v = value as? String ?: ""
                placeholder = when (column) {
                    0 -> "请输入模板名称"
                    1 -> "例如: .vue 或 vue"
                    2 -> "请输入模板类型"
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
            private val scroll = JBScrollPane(ta)

            init {
                ta.lineWrap = true
                ta.wrapStyleWord = true
                ta.font = JBTextField().font
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
        setEditorForColumn(0, textEditor)
        setEditorForColumn(1, textEditor)
        setEditorForColumn(2, textEditor)
        // column 3 = template content uses multi-line editor and renderer
        setEditorForColumn(3, multiLineEditor)
        table.columnModel.getColumn(3).cellRenderer = multiLineRenderer

        // 为文本列（0..3）设置一个占位符渲染器，使得当单元格为空且未处于编辑时显示占位提示
        val placeholders = mapOf(
            0 to "请输入模板名称",
            1 to "例如: .vue 或 vue",
            2 to "请输入模板类型",
            3 to "请输入模板内容（支持多行）"
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
        // 只为单行文本列（0..2）设置占位符渲染器，避免覆盖多行渲染器（col 3）
        for (col in 0..2) {
            table.columnModel.getColumn(col).cellRenderer = placeholderRenderer
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
                val hint = JLabel("在此配置模版。详情请查看 ")
                val url = "https://github.com/cuiwang/FileTemplatePlugin"
                val link = LinkLabel<String>("Example docs", null)
                link.setListener({ _, _ ->
                    try {
                        BrowserUtil.browse(url)
                    } catch (ex: Exception) {
                        LOG.error("打开链接失败: $url", ex)
                    }
                }, null)
                link.setToolTipText(url)
                val hintPanel = JPanel()
                hintPanel.add(hint)
                hintPanel.add(link)
                top.add(hintPanel, BorderLayout.NORTH)

                val ops = JPanel()
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

                addBtn.addActionListener {
                    // 新增一行，模板类型不设默认值，留空交由用户选择
                    tableModel.addRow(arrayOf<Any>("", "", "", "", java.lang.Boolean.TRUE))
                }

                deleteBtn.addActionListener {
                    val sel = table.selectedRow
                    if (sel >= 0) {
                        tableModel.removeRow(sel)
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
                        table.selectionModel.setSelectionInterval(sel + 1, sel + 1)
                        apply()
                    }
                }

                reset()
            } catch (ex: Exception) {
                LOG.error("Failed to create VueTemplate settings UI", ex)
                mainPanel = JPanel(BorderLayout())
                mainPanel!!.add(JLabel("加载配置页面时发生错误: ${ex.message}"), BorderLayout.CENTER)
            }
        }
        return mainPanel
    }

    override fun isModified(): Boolean {
        val stored = VueTemplateSettings.getInstance().state.templates
        if (stored.size != tableModel.rowCount) return true
        for (i in 0 until tableModel.rowCount) {
            val name = tableModel.getValueAt(i, 0) as? String ?: ""
            val suffix = tableModel.getValueAt(i, 1) as? String ?: ""
            val type = tableModel.getValueAt(i, 2) as? String ?: ""
            val content = tableModel.getValueAt(i, 3) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 4) as? Boolean ?: true
            val t = stored[i]
            if (t.name != name || t.suffix != suffix || t.type != type || t.content != content || t.enabled != enabled) return true
        }
        return false
    }

    override fun apply() {
        val s = VueTemplateSettings.getInstance().state
        s.templates.clear()
        for (i in 0 until tableModel.rowCount) {
            val name = tableModel.getValueAt(i, 0) as? String ?: ""
            val suffix = tableModel.getValueAt(i, 1) as? String ?: ""
            val type = tableModel.getValueAt(i, 2) as? String ?: ""
            val content = tableModel.getValueAt(i, 3) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 4) as? Boolean ?: true
            s.templates.add(VueTemplateSettings.Template(name, suffix, type, content, enabled))
        }
    }

    override fun reset() {
        tableModel.setRowCount(0)
        val stored = VueTemplateSettings.getInstance().state.templates
        for (t in stored) {
            tableModel.addRow(arrayOf<Any>(t.name, t.suffix, t.type, t.content, java.lang.Boolean.valueOf(t.enabled)))
        }
    }

    override fun getDisplayName(): String {
        return "File Template"
    }
}
