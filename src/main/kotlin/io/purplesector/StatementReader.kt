package io.purplesector

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatementReader {
    fun fetchStatements(months: Int): List<Statement> {
        return findStatementFiles(months).map {
            parseStatement(it)
        }
    }

    private fun findStatementFiles(months: Int): List<List<List<String>>> {
        val files = File(DOWNLOADS_DIR).listFiles { _, name -> name.contains(".csv") }.sortedByDescending { it.lastModified() }.take(months)
        return files.map { csvReader().readAll(it) }
    }

    private fun parseStatement(statement: List<List<String>>): Statement {
        return Statement(statement.drop(1).map {

            StatementEntry(LocalDate.parse(it[0], DateTimeFormatter.ofPattern("dd/MM/yyyy")), it[1], it[2], it[3], it[4], it[5])
        })
    }
}

data class StatementEntry(val date: LocalDate, val details: String, val type: String, val amountIn: String, val amountOut: String, val balance: String)
data class Statement(val entries: List<StatementEntry>)