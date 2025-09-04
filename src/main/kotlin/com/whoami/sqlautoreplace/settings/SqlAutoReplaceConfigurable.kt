package com.whoami.sqlautoreplace.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.whoami.sqlautoreplace.keymap.SqlAutoReplaceKeymapManager
import javax.swing.JComponent

/**
 * SQL自动替换插件的配置界面
 */
class SqlAutoReplaceConfigurable(private val project: Project) : Configurable {
    private var settingsComponent: SqlAutoReplaceSettingsComponent? = null

    override fun getDisplayName(): String {
        return "SQL Auto Replace"
    }

    override fun createComponent(): JComponent {
        settingsComponent = SqlAutoReplaceSettingsComponent(project)
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = SqlAutoReplaceSettings.getInstance(project)
        return settingsComponent!!.isModified(settings)
    }

    override fun apply() {
        val settings = SqlAutoReplaceSettings.getInstance(project)
        settingsComponent!!.apply(settings)
        // 快捷键设置已经在组件中直接更新到 SqlAutoReplaceKeySettings 实例中
        
        // 更新快捷键
        SqlAutoReplaceKeymapManager.getInstance(project).updateShortcut()
    }

    override fun reset() {
        val settings = SqlAutoReplaceSettings.getInstance(project)
        settingsComponent!!.reset(settings)
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}