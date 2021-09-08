package com.github.rey5137.robotrunnerplugin.configurables

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag

@State(
    name = "RobotRunProjectSettingsState",
    storages = [Storage("robot-runner-plugin__project_settings.xml")]
)
class RobotRunProjectSettingsState : PersistentStateComponent<RobotRunProjectSettingsState>, RunManagerListener {

    @OptionTag(converter = RobotRunSettingsMapConverter::class)
    var settingMap: LinkedHashMap<String, RobotRunSetting> = LinkedHashMap()

    override fun getState(): RobotRunProjectSettingsState = this

    override fun loadState(state: RobotRunProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun runConfigurationChanged(runConfigurationSetting: RunnerAndConfigurationSettings, existingId: String?) {
        if (existingId != null)
            settingMap.remove(existingId)?.let { settingMap[runConfigurationSetting.uniqueID] = it }
    }

    companion object {

        fun getInstance(project: Project): RobotRunProjectSettingsState =
            project.getService(RobotRunProjectSettingsState::class.java)
    }

    class RobotRunSettingsMapConverter : Converter<LinkedHashMap<String, RobotRunSetting>>() {

        override fun toString(value: LinkedHashMap<String, RobotRunSetting>): String {
            val gson = Gson()
            return gson.toJson(value)
        }

        override fun fromString(value: String): LinkedHashMap<String, RobotRunSetting> {
            val gson = Gson()
            val type = object : TypeToken<LinkedHashMap<String, RobotRunSetting>>() {}.type
            return gson.fromJson(value, type)
        }
    }
}
