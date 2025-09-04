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
                keyTextField.text = keyStroke.toString().replace("pressed ", "")
                keySettings.setKeyStroke(keyStroke)
                e.consume()
            }
        })

        val setKeyButton = JButton("设置快捷键")
        setKeyButton.addActionListener {
            Messages.showInfoMessage(
                "按下任意键组合设置为SQL扩展的快捷键。",
                "设置快捷键"
            )
            keyTextField.requestFocus()
        }
        keySettingPanel.add(keyTextField)
        keySettingPanel.add(setKeyButton)

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
    }

    fun reset(settings: SqlAutoReplaceSettings) {
        setAbbreviationMap(settings.abbreviationMap)
        keyTextField.text = keySettings.getKeyStrokeText()
    }
}