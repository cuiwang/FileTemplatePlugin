package com.cuiwang.vuetemplateplugin.startup

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger

class RegisterNewActionStartup : StartupActivity {
    private val LOG = Logger.getInstance(RegisterNewActionStartup::class.java)

    override fun runActivity(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            try {
                val am = ActionManager.getInstance()
                val action = am.getAction("VueTemplate.NewAction")
                val group = am.getAction("NewGroup") as? DefaultActionGroup
                LOG.info("RegisterNewActionStartup: action=${action != null}, group=${group != null}")
                if (action != null && group != null) {
                    val children = group.getChildActionsOrStubs()
                    val exists = children.any { child -> am.getId(child) == "VueTemplate.NewAction" }
                    if (!exists) {
                        group.add(action)
                        LOG.info("RegisterNewActionStartup: added VueTemplate.NewAction to NewGroup")
                    } else {
                        LOG.info("RegisterNewActionStartup: VueTemplate.NewAction already present in NewGroup")
                    }
                }
            } catch (ex: Exception) {
                LOG.error("RegisterNewActionStartup failed", ex)
            }
        }
    }
}
