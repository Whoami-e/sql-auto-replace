package com.whoami.sqlautoreplace.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.whoami.sqlautoreplace.keymap.SqlAutoReplaceKeymapManager

/**
 * 插件启动活动，确保在项目打开时正确初始化插件
 */
class SqlAutoReplaceStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // 初始化快捷键
        SqlAutoReplaceKeymapManager.getInstance(project).updateShortcut()
    }
}