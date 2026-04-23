package com.cuiwang.vuetemplateplugin.startup

import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class RegisterNewActionPostStartup : StartupActivity.DumbAware {
    private val LOG = Logger.getInstance(RegisterNewActionPostStartup::class.java)

    override fun runActivity(project: Project) {
        try {
            val am = ActionManager.getInstance()
            var action = am.getAction("FileTemplate.NewAction")
            val group = am.getAction("NewGroup") as? DefaultActionGroup
            LOG.info("RegisterNewActionPostStartup: action present=${action != null}, group present=${group != null}")
            if (action == null) {
                try {
                    val newAction = com.cuiwang.vuetemplateplugin.action.NewVueTemplateAction()
                    am.registerAction("FileTemplate.NewAction", newAction)
                    action = newAction
                    LOG.info("RegisterNewActionPostStartup: registered FileTemplate.NewAction dynamically")
                } catch (re: Exception) {
                    LOG.error("RegisterNewActionPostStartup: failed to register action dynamically", re)
                }
            }
            if (action != null && group != null) {
                val exists = group.getChildActionsOrStubs().any { child -> am.getId(child) == "FileTemplate.NewAction" }
                if (!exists) {
                    group.add(action)
                    LOG.info("RegisterNewActionPostStartup: added FileTemplate.NewAction to NewGroup")
                } else {
                    LOG.info("RegisterNewActionPostStartup: FileTemplate.NewAction already exists in NewGroup")
                }
            }
        } catch (ex: Exception) {
            LOG.error("RegisterNewActionPostStartup failed", ex)
        }
    }
}
