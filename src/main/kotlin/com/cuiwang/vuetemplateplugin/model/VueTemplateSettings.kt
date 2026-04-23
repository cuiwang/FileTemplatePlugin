package com.cuiwang.vuetemplateplugin.model

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "VueTemplateSettings", storages = [Storage("VueTemplateSettings.xml")])
class VueTemplateSettings : PersistentStateComponent<VueTemplateSettings.State> {
    data class Template(
        var name: String = "",
        var type: String = "PAGE", // PAGE or COMPONENT
        var content: String = "",
        var enabled: Boolean = true
    )

    data class State(var templates: MutableList<Template> = mutableListOf())

    private var myState: State = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.myState)
    }

    companion object {
        // fallback instance if the service isn't registered in plugin.xml (prevents NPE in UI)
        private val FALLBACK = VueTemplateSettings()

        fun getInstance(): VueTemplateSettings {
            val svc = com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(VueTemplateSettings::class.java)
            return svc ?: FALLBACK
        }
    }
}
