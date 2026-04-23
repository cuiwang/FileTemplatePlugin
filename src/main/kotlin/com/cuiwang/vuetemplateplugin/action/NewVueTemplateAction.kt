package com.cuiwang.vuetemplateplugin.action

import com.cuiwang.vuetemplateplugin.model.VueTemplateSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.diagnostic.Logger
import java.awt.BorderLayout
import javax.swing.*

class NewVueTemplateAction : AnAction("VueTemplate") {
    private val LOG = Logger.getInstance(NewVueTemplateAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread {
        // Ensure update() runs on background thread so retrieving data keys is safe
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        // This runs on background thread now
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = vf != null && vf.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val view = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        LOG.info("NewVueTemplateAction invoked for: ${view.path}")
        val dialog = CreateTemplateDialog(project, view)
        if (dialog.showAndGet()) {
            val fileName = dialog.fileNameField.text.trim()
            val template = dialog.getSelectedTemplate() ?: run {
                Messages.showErrorDialog(project, "请先选择一个模板", "错误")
                return
            }
            if (fileName.isEmpty()) {
                Messages.showErrorDialog(project, "请输入文件名", "错误")
                return
            }
            createFile(view, fileName, template.content)
        }
    }

    private fun createFile(target: VirtualFile, fileName: String, content: String) {
        val dir = if (target.isDirectory) target else target.parent
        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
            try {
                val vf = dir.createChildData(this, fileName)
                VfsUtil.saveText(vf, content)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    class CreateTemplateDialog(val project: com.intellij.openapi.project.Project, val target: VirtualFile) : DialogWrapper(project) {
        val typeCombo = JComboBox(arrayOf("PAGE", "COMPONENT"))
        val templateList = JList<String>()
        val fileNameField = JTextField(30)

        init {
            init()
            title = "Create Vue Template"
            updateTemplateList()
            typeCombo.addActionListener {
                updateTemplateList()
            }
        }

        private fun updateTemplateList() {
            val type = typeCombo.selectedItem as String
            val templates = VueTemplateSettings.getInstance().state.templates.filter { it.type == type && it.enabled }
            val names = templates.map { it.name }
            templateList.model = DefaultListModel<String>().also { model -> names.forEach { model.addElement(it) } }
        }

        override fun createCenterPanel(): JComponent? {
            val p = JPanel(BorderLayout())
            val top = JPanel()
            top.add(JLabel("类型:"))
            top.add(typeCombo)
            p.add(top, BorderLayout.NORTH)

            val center = JPanel(BorderLayout())
            center.add(JScrollPane(templateList), BorderLayout.CENTER)
            p.add(center, BorderLayout.CENTER)

            val bottom = JPanel()
            bottom.add(JLabel("文件名:"))
            bottom.add(fileNameField)
            p.add(bottom, BorderLayout.SOUTH)

            return p
        }

        fun getSelectedTemplate(): VueTemplateSettings.Template? {
            val sel = templateList.selectedValue ?: return null
            return VueTemplateSettings.getInstance().state.templates.find { it.name == sel }
        }
    }
}
