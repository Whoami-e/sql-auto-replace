package com.whoami.sqlautoreplace.keymap

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.components.Service
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
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
            if (keyStroke != null) {
                val actionManager = ActionManager.getInstance()
                val actionId = "com.whoami.sqlautoreplace.action.SqlAutoReplaceAction"
                val action = actionManager.getAction(actionId)
                if (action != null) {
                    val keymap = KeymapManager.getInstance().activeKeymap
                    updateKeymapShortcut(keymap, actionId, keyStroke)
                }
            }
        } catch (e: Exception) {
            // 处理异常情况
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