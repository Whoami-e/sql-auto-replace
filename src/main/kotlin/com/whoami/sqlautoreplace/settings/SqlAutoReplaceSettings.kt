package com.whoami.sqlautoreplace.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 存储SQL缩写和对应的完整SQL代码的映射关系
 */
@State(
    name = "SqlAutoReplaceSettings",
    storages = [Storage("sqlAutoReplaceSettings.xml")]
)
class SqlAutoReplaceSettings : PersistentStateComponent<SqlAutoReplaceSettings> {
    var abbreviationMap: MutableMap<String, String> = mutableMapOf(
        "sf" to "SELECT * FROM ",
        "scf" to "SELECT COUNT(*) FROM ",
        "ij" to "INNER JOIN ",
        "lj" to "LEFT JOIN ",
        "rj" to "RIGHT JOIN ",
        "gb" to "GROUP BY ",
        "ob" to "ORDER BY ",
        "wh" to "WHERE "
    )

    override fun getState(): SqlAutoReplaceSettings {
        return this
    }

    override fun loadState(state: SqlAutoReplaceSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): SqlAutoReplaceSettings {
            return project.getService(SqlAutoReplaceSettings::class.java)
        }
    }
}