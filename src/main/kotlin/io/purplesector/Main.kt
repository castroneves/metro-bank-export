package io.purplesector

const val MONTHS_BACK: Int = 1
const val CHROMEDRIVER_URI = "/home/user/bin/chromedriver" // If Linux/BSD
const val DOWNLOADS_DIR: String = "C:\\Users\\username\\Downloads" // If windows
const val METROBANK_CUSTOMER_NUMER = ""
const val METROBANK_PASSCODE: String = ""
const val METROBANK_PASSWORD: String = ""
const val FREEAGENT_BANK_ACCOUNT_ID: String = ""
const val FREEAGENT_API_KEY = ""
const val FREEAGENT_API_SECRET = ""

fun main() {
    MetrobankStatementGrabber().downloadStatements()
    val statements: List<Statement> = StatementReader().fetchStatements(MONTHS_BACK)
    FreeAgentClient().transformAndSendStatements(statements, FREEAGENT_BANK_ACCOUNT_ID)
    FreeAgentClient.httpService.stop()
}