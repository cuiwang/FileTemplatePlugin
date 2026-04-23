package com.cuiwang.vuetemplateplugin.startup

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.application.ApplicationManager

class RegisterNewActionStartup : ProjectActivity {
    private val LOG = Logger.getInstance(RegisterNewActionStartup::class.java)

    override suspend fun execute(project: Project) {
        // Run registration on EDT after project opened
        ApplicationManager.getApplication().invokeLater {
            try {
                val am = ActionManager.getInstance()
                var action = am.getAction("FileTemplate.NewAction")
                val group = am.getAction("NewGroup") as? DefaultActionGroup
                LOG.info("RegisterNewActionStartup: action present=${action != null}, group present=${group != null}")
                if (action == null) {
                    // Register a new instance of our action if it's not present
                    try {
                        val newAction = com.cuiwang.vuetemplateplugin.action.NewVueTemplateAction()
                        am.registerAction("FileTemplate.NewAction", newAction)
                        action = newAction
                        LOG.info("RegisterNewActionStartup: registered FileTemplate.NewAction dynamically")
                    } catch (re: Exception) {
                        LOG.error("RegisterNewActionStartup: failed to register action dynamically", re)
                    }
                }
                if (action != null && group != null) {
                    val children = group.getChildActionsOrStubs()
                    val exists = children.any { child -> am.getId(child) == "FileTemplate.NewAction" }
                    if (!exists) {
                        group.add(action)
                        LOG.info("RegisterNewActionStartup: added FileTemplate.NewAction to NewGroup")
                    } else {
                        LOG.info("RegisterNewActionStartup: FileTemplate.NewAction already exists in NewGroup")
                    }
                }
            } catch (ex: Exception) {
                LOG.error("RegisterNewActionStartup failed", ex)
            }
        }
    }
}
