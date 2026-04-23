package com.cuiwang.vuetemplateplugin.action

import com.cuiwang.vuetemplateplugin.model.VueTemplateSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import javax.swing.*
import java.awt.BorderLayout
import java.awt.FlowLayout

/**
 * Action that adds a "File Template" entry to the New menu and shows a dialog to create a file from a selected template.
 */
class NewVueTemplateAction : AnAction() {
    private val LOG = Logger.getInstance(NewVueTemplateAction::class.java)

    init {
        // Set presentation text and icon
        templatePresentation.text = "File Template"
        try {
            templatePresentation.icon = IconLoader.getIcon("/META-INF/pluginIcon.svg", javaClass)
        } catch (_: Exception) {
        }

        // Ensure the action is added to NewGroup at runtime (safe on EDT)
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            try {
                val am = com.intellij.openapi.actionSystem.ActionManager.getInstance()
                val action = am.getAction("FileTemplate.NewAction")
                val group = am.getAction("NewGroup") as? com.intellij.openapi.actionSystem.DefaultActionGroup
                if (action != null && group != null) {
                    val exists = group.getChildActionsOrStubs().any { child -> am.getId(child) == "FileTemplate.NewAction" }
                    if (!exists) {
                        group.add(action)
                        LOG.info("NewVueTemplateAction: added File Template action to NewGroup at runtime")
                    } else {
                        LOG.info("NewVueTemplateAction: File Template already present in NewGroup")
                    }
                } else {
                    LOG.info("NewVueTemplateAction: action or NewGroup not available yet: action=${action!=null}, group=${group!=null}")
                }
            } catch (ex: Exception) {
                LOG.error("NewVueTemplateAction: failed to add action to NewGroup", ex)
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        // Only enable/visible when a directory is selected in Project view
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = vf != null && vf.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val view = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val dialog = CreateTemplateDialog(project)
        if (dialog.showAndGet()) {
            val userFileName = dialog.fileNameField.text.trim()
            val template = dialog.getSelectedTemplate() ?: run {
                Messages.showErrorDialog(project, "Please select a template.", "Error")
                return
            }
            if (userFileName.isEmpty()) {
                Messages.showErrorDialog(project, "Please enter a file name.", "Error")
                return
            }
            val actualFileName = resolveFileNameWithSuffix(userFileName, template.suffix)
            createFile(view, actualFileName, template.content)
        }
    }

    private fun resolveFileNameWithSuffix(userName: String, templateSuffix: String): String {
        if (userName.contains('.') && !userName.endsWith('.')) return userName
        if (templateSuffix.isBlank()) return userName
        return if (templateSuffix.startsWith('.')) userName + templateSuffix else "${userName}.${templateSuffix}"
    }

    private fun createFile(target: VirtualFile, fileName: String, content: String) {
        val dir = if (target.isDirectory) target else target.parent
        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
            try {
                val vf = dir.createChildData(this, fileName)
                VfsUtil.saveText(vf, content)
            } catch (ex: Exception) {
                LOG.error("创建文件失败: $fileName", ex)
            }
        }
    }

    /**
     * Dialog that shows available template types and templates (types come from settings dynamically).
     */
    class CreateTemplateDialog(project: com.intellij.openapi.project.Project) : DialogWrapper(project) {
        // types and templates are loaded from settings
        val typeCombo = ComboBox<String>()
        val templateList = JBList<String>()
        val fileNameField = JTextField(30)

        init {
            init()
            title = "Create File Template"
            loadTypes()
            typeCombo.addActionListener { updateTemplateList() }
        }

        private fun loadTypes() {
            // 从设置中收集所有不同的 type 值
            val types = VueTemplateSettings.getInstance().state.templates.map { it.type }.distinct()
            // 如果 types 为空，不添加默认类型，保留为空以提示用户先在设置中添加模板类型
            types.forEach { typeCombo.addItem(it) }
            updateTemplateList()
        }

        private fun updateTemplateList() {
            val type = (typeCombo.selectedItem as? String) ?: return
            val templates = VueTemplateSettings.getInstance().state.templates.filter { it.type == type && it.enabled }
            val names = templates.map { it.name }
            val model = DefaultListModel<String>()
            names.forEach { model.addElement(it) }
            templateList.model = model
            // 默认选中第一个模板（如果有），以减少用户操作
            if (model.size() > 0) {
                templateList.setSelectedIndex(0)
                templateList.ensureIndexIsVisible(0)
            }
        }

        override fun createCenterPanel(): JComponent {
            val p = JPanel(BorderLayout(8, 8))
            p.border = JBUI.Borders.empty(10)

            // top: icon + type select
            val top = JPanel(FlowLayout(FlowLayout.LEFT))
            val iconLabel = JLabel()
            try {
                val ic = IconLoader.getIcon("/META-INF/pluginIcon.svg", javaClass)
                iconLabel.icon = ic
                iconLabel.preferredSize = java.awt.Dimension(16, 16)
            } catch (_: Exception) {
                // ignore icon loading errors
            }
            top.add(iconLabel)
            top.add(JLabel("类型:"))
            top.add(typeCombo)
            p.add(top, BorderLayout.NORTH)

            val center = JPanel(BorderLayout())
            center.add(JScrollPane(templateList), BorderLayout.CENTER)
            p.add(center, BorderLayout.CENTER)

            val bottom = JPanel(FlowLayout(FlowLayout.LEFT))
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
