package com.whoami.sqlautoreplace.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.whoami.sqlautoreplace.settings.SqlAutoReplaceSettings

/**
 * SQL自动替换动作，当用户按下快捷键时触发
 */
class SqlAutoReplaceAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val caretModel = editor.caretModel
        
        // 直接执行替换
        expandAbbreviation(project, editor, caretModel)
    }

    private fun expandAbbreviation(project: Project, editor: Editor, caretModel: CaretModel) {
        val document = editor.document
        val settings = SqlAutoReplaceSettings.getInstance(project)
        val abbreviationMap = settings.abbreviationMap

        // 获取当前光标位置
        val offset = caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset))

        // 从光标位置向前查找可能的缩写
        val textBeforeCaret = lineText.substring(0, offset - lineStartOffset)
        val possibleAbbreviation = findPossibleAbbreviation(textBeforeCaret)

        // 检查是否找到了有效的缩写（忽略大小写）
        if (possibleAbbreviation.isNotEmpty()) {
            // 查找匹配的缩写（忽略大小写）
            val matchingEntry = abbreviationMap.entries.find { 
                it.key.equals(possibleAbbreviation, ignoreCase = true) 
            }
            
            if (matchingEntry != null) {
                val expansion = matchingEntry.value
                val abbreviationStartOffset = offset - possibleAbbreviation.length

                // 在写命令中执行替换操作
                WriteCommandAction.runWriteCommandAction(project) {
                    document.replaceString(abbreviationStartOffset, offset, expansion)
                    // 移动光标到替换后的位置
                    caretModel.moveToOffset(abbreviationStartOffset + expansion.length)
                }
            }
        }
    }

    /**
     * 从光标前的文本中查找可能的缩写
     */
    private fun findPossibleAbbreviation(text: String): String {
        // 从文本末尾向前查找，直到遇到空格或其他分隔符
        var i = text.length - 1
        while (i >= 0) {
            val char = text[i]
            if (!char.isLetterOrDigit() && char != '_') {
                break
            }
            i--
        }
        return text.substring(i + 1)
    }
}