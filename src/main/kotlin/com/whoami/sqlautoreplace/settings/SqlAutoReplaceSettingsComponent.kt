package com.whoami.sqlautoreplace.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * SQL自动替换插件的设置界面组件
 */
class SqlAutoReplaceSettingsComponent(private val project: Project) {
    val panel: JPanel = JPanel(BorderLayout())
    private val tableModel = createTableModel()
    private val table = JBTable(tableModel)
    private val keySettingPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    private val keyTextField = JTextField(15)
    private val keySettings = SqlAutoReplaceKeySettings.getInstance(project)

    init {
        // 设置表格
        table.setShowGrid(true)
        table.columnModel.getColumn(0).preferredWidth = 150
        table.columnModel.getColumn(1).preferredWidth = 350

        val toolbarDecorator = ToolbarDecorator.createDecorator(table)
            .setAddAction { addAbbreviation() }
            .setRemoveAction { removeSelectedAbbreviation() }
            .disableUpDownActions()

        // 创建快捷键设置面板
        keySettingPanel.add(JLabel("快捷键: "))
        keyTextField.isEditable = false
        keyTextField.text = keySettings.getKeyStrokeText()
        keyTextField.preferredSize = Dimension(150, 30)
        keyTextField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                val keyStroke = KeyStroke.getKeyStrokeForEvent(e)
                
                // 检查快捷键是否可能与系统冲突
                if (keySettings.isPotentialKeyConflict(keyStroke)) {
                    val osName = System.getProperty("os.name")
                    val isMac = osName.toLowerCase().contains("mac")
                    val isWindows = osName.toLowerCase().contains("win")
                    val isLinux = osName.toLowerCase().contains("linux")
                    
                    val message = if (isMac) {
                        "警告：您选择的快捷键在macOS上可能与系统功能或特殊字符输入冲突。\n" +
                        "这可能导致快捷键无法正常工作或触发系统功能。\n" +
                        "建议选择其他快捷键组合，或者使用非扩展输入法。\n\n" +
                        "是否仍要使用此快捷键？"
                    } else if (isWindows) {
                        "警告：您选择的快捷键在Windows上可能与系统快捷键冲突。\n" +
                        "这可能导致快捷键无法正常工作或触发系统功能。\n" +
                        "建议选择其他快捷键组合。\n\n" +
                        "是否仍要使用此快捷键？"
                    } else if (isLinux) {
                        "警告：您选择的快捷键在Linux上可能与系统快捷键冲突。\n" +
                        "这可能导致快捷键无法正常工作或触发系统功能。\n" +
                        "建议选择其他快捷键组合。\n\n" +
                        "是否仍要使用此快捷键？"
                    } else {
                        "警告：您选择的快捷键可能与系统快捷键或IDE功能冲突。\n" +
                        "这可能导致快捷键无法正常工作或触发其他功能。\n" +
                        "是否仍要使用此快捷键？"
                    }
                    
                    val result = Messages.showYesNoDialog(
                        project,
                        message,
                        "快捷键冲突警告",
                        Messages.getWarningIcon()
                    )
                    
                    if (result == Messages.YES) {
                        // 用户确认使用此快捷键
                        keyTextField.text = keyStroke.toString().replace("pressed ", "")
                        keySettings.setKeyStroke(keyStroke)
                        // 立即保存状态
                        keySettings.getState()
                    }
                } else {
                    // 没有冲突，直接设置
                    keyTextField.text = keyStroke.toString().replace("pressed ", "")
                    keySettings.setKeyStroke(keyStroke)
                    // 立即保存状态
                    keySettings.getState()
                }
                
                e.consume()
            }
        })

        // 移除了设置快捷键按钮，用户可以直接点击文本框设置快捷键
        keyTextField.toolTipText = "点击此处并按下任意键组合设置为SQL扩展的快捷键"
        keySettingPanel.add(keyTextField)

        // 添加到主面板
        val topPanel = JPanel(BorderLayout())
        topPanel.add(JLabel("配置SQL缩写及其扩展:"), BorderLayout.NORTH)
        topPanel.add(keySettingPanel, BorderLayout.CENTER)

        panel.add(topPanel, BorderLayout.NORTH)
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER)
    }

    private fun createTableModel(): DefaultTableModel {
        val model = DefaultTableModel()
        model.addColumn("Abbreviation")
        model.addColumn("SQL Expansion")
        return model
    }

    private fun addAbbreviation() {
        val abbreviation = JOptionPane.showInputDialog(panel, "Enter abbreviation:")
        if (abbreviation != null && abbreviation.isNotEmpty()) {
            val expansion = JOptionPane.showInputDialog(panel, "Enter SQL expansion:")
            if (expansion != null && expansion.isNotEmpty()) {
                tableModel.addRow(arrayOf(abbreviation, expansion))
            }
        }
    }

    private fun removeSelectedAbbreviation() {
        val selectedRow = table.selectedRow
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow)
        }
    }

    fun setAbbreviationMap(abbreviationMap: Map<String, String>) {
        tableModel.rowCount = 0
        for ((abbreviation, expansion) in abbreviationMap) {
            tableModel.addRow(arrayOf(abbreviation, expansion))
        }
    }

    fun getAbbreviationMap(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until tableModel.rowCount) {
            val abbreviation = tableModel.getValueAt(i, 0) as String
            val expansion = tableModel.getValueAt(i, 1) as String
            map[abbreviation] = expansion
        }
        return map
    }

    fun isModified(settings: SqlAutoReplaceSettings): Boolean {
        val currentMap = getAbbreviationMap()
        val storedMap = settings.abbreviationMap
        
        // 检查快捷键是否被修改
        val isKeyModified = keySettings.keyStroke != keySettings.getState().keyStroke

        return currentMap != storedMap || isKeyModified
    }

    fun apply(settings: SqlAutoReplaceSettings) {
        settings.abbreviationMap = getAbbreviationMap()
        // 确保快捷键设置被保存
        keySettings.getState() // 触发状态保存
    }

    fun reset(settings: SqlAutoReplaceSettings) {
        setAbbreviationMap(settings.abbreviationMap)
        keyTextField.text = keySettings.getKeyStrokeText()
    }
}