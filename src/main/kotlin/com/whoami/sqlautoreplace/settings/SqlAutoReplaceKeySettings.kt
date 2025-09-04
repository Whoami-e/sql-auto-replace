package com.whoami.sqlautoreplace.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
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
     * 检查快捷键是否可能与系统特殊字符输入或系统功能冲突
     * 返回值：true表示可能存在冲突，false表示没有冲突
     */
    fun isPotentialKeyConflict(keyStroke: KeyStroke): Boolean {
        val modifiers = keyStroke.modifiers
        val keyCode = keyStroke.keyCode
        
        // 检查操作系统类型
        val osName = System.getProperty("os.name").toLowerCase()
        val isMac = osName.contains("mac")
        val isWindows = osName.contains("win")
        val isLinux = osName.contains("linux")
        
        // 检查是否包含各种修饰键
        val hasAlt = (modifiers and InputEvent.ALT_DOWN_MASK) != 0 || 
                     (modifiers and InputEvent.ALT_MASK) != 0
        val hasCtrl = (modifiers and InputEvent.CTRL_DOWN_MASK) != 0 || 
                      (modifiers and InputEvent.CTRL_MASK) != 0
        val hasShift = (modifiers and InputEvent.SHIFT_DOWN_MASK) != 0 || 
                       (modifiers and InputEvent.SHIFT_MASK) != 0
        val hasMeta = (modifiers and InputEvent.META_DOWN_MASK) != 0 || 
                      (modifiers and InputEvent.META_MASK) != 0
        
        // 检查通用的系统快捷键冲突
        if (isSystemWideShortcut(keyStroke, isMac, isWindows, isLinux)) {
            return true
        }
        
        // 检查特定操作系统的冲突
        if (isMac) {
            // macOS特有的冲突检查
            if (hasAlt) {
                // macOS上与Alt/Option组合时会产生特殊字符的键
                val macAltConflictKeys = listOf(
                    KeyEvent.VK_EQUALS,      // Alt+= (≠)
                    KeyEvent.VK_8,          // Alt+8 (•)
                    KeyEvent.VK_9,          // Alt+9 (ª)
                    KeyEvent.VK_0,          // Alt+0 (º)
                    KeyEvent.VK_MINUS,      // Alt+- (–)
                    KeyEvent.VK_BACK_SLASH, // Alt+\ («)
                    KeyEvent.VK_OPEN_BRACKET, // Alt+[ (")
                    KeyEvent.VK_CLOSE_BRACKET, // Alt+] (')
                    KeyEvent.VK_QUOTE,      // Alt+' (æ)
                    KeyEvent.VK_SEMICOLON,   // Alt+; (…)
                    // 添加更多macOS上Alt键的特殊字符
                    KeyEvent.VK_A,          // Alt+a (å)
                    KeyEvent.VK_E,          // Alt+e (´)
                    KeyEvent.VK_I,          // Alt+i (ˆ)
                    KeyEvent.VK_N,          // Alt+n (˜)
                    KeyEvent.VK_U,          // Alt+u (¨)
                    KeyEvent.VK_G,          // Alt+g (©)
                    KeyEvent.VK_R,          // Alt+r (®)
                    KeyEvent.VK_2,          // Alt+2 (™)
                    KeyEvent.VK_PERIOD,     // Alt+. (≥)
                    KeyEvent.VK_COMMA       // Alt+, (≤)
                )
                if (macAltConflictKeys.contains(keyCode)) {
                    return true
                }
            }
            
            if (hasMeta) { // Command键
                // macOS上Command键的系统快捷键
                val macCommandConflictKeys = listOf(
                    KeyEvent.VK_Q,          // Command+Q (退出应用)
                    KeyEvent.VK_W,          // Command+W (关闭窗口)
                    KeyEvent.VK_H,          // Command+H (隐藏应用)
                    KeyEvent.VK_M,          // Command+M (最小化窗口)
                    KeyEvent.VK_SPACE,      // Command+Space (Spotlight搜索)
                    KeyEvent.VK_TAB         // Command+Tab (切换应用)
                )
                if (macCommandConflictKeys.contains(keyCode)) {
                    return true
                }
            }
        } else if (isWindows) {
            // Windows特有的冲突检查
            if (hasAlt) {
                // Windows上可能有冲突的Alt组合键
                val winAltConflictKeys = listOf(
                    KeyEvent.VK_F4,         // Alt+F4 (关闭窗口)
                    KeyEvent.VK_TAB,        // Alt+Tab (切换窗口)
                    KeyEvent.VK_SPACE,      // Alt+Space (系统菜单)
                    KeyEvent.VK_ENTER,      // Alt+Enter (属性)
                    KeyEvent.VK_ESCAPE,     // Alt+Esc (切换任务)
                    KeyEvent.VK_PRINTSCREEN // Alt+PrintScreen (当前窗口截图)
                )
                if (winAltConflictKeys.contains(keyCode)) {
                    return true
                }
            }
            
            if (hasCtrl && hasAlt) {
                // Ctrl+Alt组合键
                val ctrlAltConflictKeys = listOf(
                    KeyEvent.VK_DELETE,     // Ctrl+Alt+Delete (任务管理器)
                    KeyEvent.VK_TAB         // Ctrl+Alt+Tab (切换任务视图)
                )
                if (ctrlAltConflictKeys.contains(keyCode)) {
                    return true
                }
            }
        } else if (isLinux) {
            // Linux特有的冲突检查
            if (hasAlt) {
                val linuxAltConflictKeys = listOf(
                    KeyEvent.VK_TAB,        // Alt+Tab (切换窗口)
                    KeyEvent.VK_F4,         // Alt+F4 (关闭窗口)
                    KeyEvent.VK_F2          // Alt+F2 (运行命令)
                )
                if (linuxAltConflictKeys.contains(keyCode)) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 检查是否是跨平台的系统级快捷键
     */
    private fun isSystemWideShortcut(keyStroke: KeyStroke, isMac: Boolean, isWindows: Boolean, isLinux: Boolean): Boolean {
        val modifiers = keyStroke.modifiers
        val keyCode = keyStroke.keyCode
        
        // 检查是否包含各种修饰键
        val hasAlt = (modifiers and InputEvent.ALT_DOWN_MASK) != 0 || 
                     (modifiers and InputEvent.ALT_MASK) != 0
        val hasCtrl = (modifiers and InputEvent.CTRL_DOWN_MASK) != 0 || 
                      (modifiers and InputEvent.CTRL_MASK) != 0
        val hasShift = (modifiers and InputEvent.SHIFT_DOWN_MASK) != 0 || 
                       (modifiers and InputEvent.SHIFT_MASK) != 0
        val hasMeta = (modifiers and InputEvent.META_DOWN_MASK) != 0 || 
                      (modifiers and InputEvent.META_MASK) != 0
        
        // 通用的IDE快捷键冲突
        if (hasCtrl || (isMac && hasMeta)) {
            // 在Windows/Linux上是Ctrl，在Mac上是Command
            val primaryModifierConflictKeys = listOf(
                KeyEvent.VK_C,          // 复制
                KeyEvent.VK_V,          // 粘贴
                KeyEvent.VK_X,          // 剪切
                KeyEvent.VK_Z,          // 撤销
                KeyEvent.VK_S,          // 保存
                KeyEvent.VK_O,          // 打开
                KeyEvent.VK_N,          // 新建
                KeyEvent.VK_P,          // 打印
                KeyEvent.VK_F,          // 查找
                KeyEvent.VK_A           // 全选
            )
            if (primaryModifierConflictKeys.contains(keyCode)) {
                return true
            }
        }
        
        // 功能键冲突
        val functionKeyConflicts = listOf(
            KeyEvent.VK_F1,         // 帮助
            KeyEvent.VK_F11         // 全屏
        )
        if (functionKeyConflicts.contains(keyCode)) {
            return true
        }
        
        return false
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