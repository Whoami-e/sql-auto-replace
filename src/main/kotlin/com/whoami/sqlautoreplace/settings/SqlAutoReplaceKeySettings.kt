package com.whoami.sqlautoreplace.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import javax.swing.KeyStroke

/**
 * 存储SQL自动替换的快捷键设置
 */
@State(
    name = "SqlAutoReplaceKeySettings",
    storages = [Storage("sqlAutoReplaceKeySettings.xml")]
)
class SqlAutoReplaceKeySettings : PersistentStateComponent<SqlAutoReplaceKeySettings> {
    // 默认快捷键为 Alt+;
    var keyStroke: String = "alt SEMICOLON"
    
    // 触发模式：始终使用单击触发
    var doubleClickMode: Boolean = false // 保留属性但不再使用

    override fun getState(): SqlAutoReplaceKeySettings {
        return this
    }

    override fun loadState(state: SqlAutoReplaceKeySettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    /**
     * 获取当前快捷键的可读表示
     */
    fun getKeyStrokeText(): String {
        return try {
            val stroke = KeyStroke.getKeyStroke(keyStroke)
            if (stroke != null) {
                KeymapUtil.getKeystrokeText(stroke)
            } else {
                "Alt+;"
            }
        } catch (e: Exception) {
            "Alt+;"
        }
    }

    /**
     * 设置快捷键
     */
    fun setKeyStroke(keyStroke: KeyStroke) {
        this.keyStroke = keyStroke.toString().replace("pressed ", "")
    }

    /**
     * 获取KeyStroke对象
     */
    fun getKeyStroke(): KeyStroke? {
        return KeyStroke.getKeyStroke(keyStroke)
    }

    companion object {
        fun getInstance(project: Project): SqlAutoReplaceKeySettings {
            return project.getService(SqlAutoReplaceKeySettings::class.java)
        }
    }
}