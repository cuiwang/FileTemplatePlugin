package com.cuiwang.vuetemplateplugin.ui

import com.cuiwang.vuetemplateplugin.model.VueTemplateSettings
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.diagnostic.Logger
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.awt.Dimension

class VueTemplateConfigurable : Configurable {
    private val LOG = Logger.getInstance(VueTemplateConfigurable::class.java)
    private var mainPanel: JPanel? = null
    private val columnNames = arrayOf("模板名称", "模板类型", "模板内容", "是否启用")
    private val tableModel = object : DefaultTableModel(columnNames, 0) {
        override fun getColumnClass(column: Int): Class<*> {
            return when (column) {
                3 -> java.lang.Boolean::class.java
                else -> String::class.java
            }
        }
    }
    private val table = JTable(tableModel)

    init {
        table.setRowHeight(24)
        table.columnModel.getColumn(2).preferredWidth = 400
    }

    override fun createComponent(): JComponent? {
        if (mainPanel == null) {
            try {
                mainPanel = JPanel(BorderLayout())

                val top = JPanel(BorderLayout())
                val hint = JLabel("在此配置模版。详情请查看 ")
                val link = JLabel("<html><a href=\"https://example.com\">示例文档</a></html>")
                val hintPanel = JPanel()
                hintPanel.add(hint)
                hintPanel.add(link)
                top.add(hintPanel, BorderLayout.NORTH)

                val ops = JPanel()
                val addBtn = JButton("新增")
                val uploadBtn = JButton("上传")
                val deleteBtn = JButton("删除")
                val upBtn = JButton("上移")
                val downBtn = JButton("下移")
                ops.add(addBtn)
                ops.add(uploadBtn)
                ops.add(deleteBtn)
                ops.add(upBtn)
                ops.add(downBtn)
                top.add(ops, BorderLayout.SOUTH)

                mainPanel!!.add(top, BorderLayout.NORTH)

                val scroll = JScrollPane(table)
                scroll.preferredSize = Dimension(700, 300)
                mainPanel!!.add(scroll, BorderLayout.CENTER)

                addBtn.addActionListener {
                    tableModel.addRow(arrayOf<Any>("", "PAGE", "", java.lang.Boolean.TRUE))
                }

                uploadBtn.addActionListener {
                    // TODO: implement uploading file to import templates
                }

                deleteBtn.addActionListener {
                    val sel = table.selectedRow
                    if (sel >= 0) {
                        tableModel.removeRow(sel)
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
            val type = tableModel.getValueAt(i, 1) as? String ?: "PAGE"
            val content = tableModel.getValueAt(i, 2) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 3) as? Boolean ?: true
            val t = stored[i]
            if (t.name != name || t.type != type || t.content != content || t.enabled != enabled) return true
        }
        return false
    }

    override fun apply() {
        val s = VueTemplateSettings.getInstance().state
        s.templates.clear()
        for (i in 0 until tableModel.rowCount) {
            val name = tableModel.getValueAt(i, 0) as? String ?: ""
            val type = tableModel.getValueAt(i, 1) as? String ?: "PAGE"
            val content = tableModel.getValueAt(i, 2) as? String ?: ""
            val enabled = tableModel.getValueAt(i, 3) as? Boolean ?: true
            s.templates.add(VueTemplateSettings.Template(name, type, content, enabled))
        }
    }

    override fun reset() {
        tableModel.setRowCount(0)
        val stored = VueTemplateSettings.getInstance().state.templates
        for (t in stored) {
            tableModel.addRow(arrayOf<Any>(t.name, t.type, t.content, java.lang.Boolean.valueOf(t.enabled)))
        }
    }

    override fun getDisplayName(): String {
        return "VueTemplate"
    }
}
