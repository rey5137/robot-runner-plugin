package com.github.rey5137.robotrunnerplugin.configurables

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
class RobotRunProjectSettingsState: PersistentStateComponent<RobotRunProjectSettingsState> {

    @OptionTag(converter = RobotRunSettingsConverter::class)
    var settings: MutableList<RobotRunSetting> = mutableListOf()

    override fun getState(): RobotRunProjectSettingsState = this

    override fun loadState(state: RobotRunProjectSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {

        fun getInstance(project: Project): RobotRunProjectSettingsState =
            project.getService(RobotRunProjectSettingsState::class.java)

    }

    class RobotRunSettingsConverter : Converter<MutableList<RobotRunSetting>>() {

        override fun toString(value: MutableList<RobotRunSetting>): String {
            val gson = Gson()
            return gson.toJson(value)
        }

        override fun fromString(value: String): MutableList<RobotRunSetting> {
            val gson = Gson()
            val type = object : TypeToken<MutableList<RobotRunSetting>>() {}.type
            return gson.fromJson(value, type)
        }

    }


}