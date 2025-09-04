package com.whoami.sqlautoreplace.keymap

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.Service
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.whoami.sqlautoreplace.settings.SqlAutoReplaceKeySettings
import javax.swing.KeyStroke

/**
 * 管理SQL自动替换的快捷键
 */
@Service
class SqlAutoReplaceKeymapManager(private val project: Project) {

    /**
     * 更新SQL自动替换动作的快捷键
     */
    fun updateShortcut() {
        val keySettings = SqlAutoReplaceKeySettings.getInstance(project)
        val keyStroke = keySettings.getKeyStroke()
        
        try {
            val actionManager = ActionManager.getInstance()
            val actionId = "com.whoami.sqlautoreplace.action.SqlAutoReplaceAction"
            val action = actionManager.getAction(actionId)
            if (action != null) {
                val keymap = KeymapManager.getInstance().activeKeymap
                if (keyStroke != null) {
                    // 检查快捷键是否可能与系统冲突
                    if (keySettings.isPotentialKeyConflict(keyStroke)) {
                        // 记录可能的冲突，但仍然应用用户的选择
                        // 因为用户在设置界面已经被警告并确认使用此快捷键
                        println("警告：使用可能与系统冲突的快捷键: ${KeymapUtil.getKeystrokeText(keyStroke)}")
                    }
                    updateKeymapShortcut(keymap, actionId, keyStroke)
                } else {
                    // 如果没有设置快捷键，使用默认的 Alt+;
                    val defaultKeyStroke = KeyStroke.getKeyStroke("alt SEMICOLON")
                    if (defaultKeyStroke != null) {
                        updateKeymapShortcut(keymap, actionId, defaultKeyStroke)
                    }
                }
            }
        } catch (e: Exception) {
            // 处理异常情况，使用默认快捷键
            val defaultKeyStroke = KeyStroke.getKeyStroke("alt SEMICOLON")
            if (defaultKeyStroke != null) {
                val actionId = "com.whoami.sqlautoreplace.action.SqlAutoReplaceAction"
                val keymap = KeymapManager.getInstance().activeKeymap
                updateKeymapShortcut(keymap, actionId, defaultKeyStroke)
            }
        }
    }

    /**
     * 更新指定动作的快捷键
     */
    private fun updateKeymapShortcut(keymap: Keymap, actionId: String, keyStroke: KeyStroke) {
        // 移除现有的快捷键
        val existingShortcuts = keymap.getShortcuts(actionId)
        for (shortcut in existingShortcuts) {
            keymap.removeShortcut(actionId, shortcut)
        }

        // 添加新的快捷键（单击快捷键，双击逻辑在Action中处理）
        val shortcut = KeyboardShortcut(keyStroke, null)
        keymap.addShortcut(actionId, shortcut)
    }

    companion object {
        fun getInstance(project: Project): SqlAutoReplaceKeymapManager {
            return project.getService(SqlAutoReplaceKeymapManager::class.java)
        }
    }
}