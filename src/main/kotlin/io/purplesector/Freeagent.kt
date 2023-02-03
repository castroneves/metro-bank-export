package io.purplesector

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth20Service
import org.yaml.snakeyaml.Yaml
import spark.Service
import java.io.File
import java.time.Instant
import java.util.concurrent.CountDownLatch

class FreeAgentClient {
    val PORT = 8080
    val latch: CountDownLatch = CountDownLatch(1)

    @Volatile
    var authCode: String? = null

    companion object {
        val httpService = Service.ignite()
    }

    init {
        httpService.port(PORT)
        httpService.get("/auth") { req, res ->
            val code = req.queryParams("code")
            authCode = code
            latch.countDown()
            res.body("Login success")
            res.body()
        }
        httpService.awaitInitialization()
        println("server running")
    }

    fun transformAndSendStatements(statements: List<Statement>, bankAccountId: String) {

        val (oauthService, accessToken) = auth()
//        val transactions = statements.flatMap { it.entries }.toSet().plus(StatementEntry(LocalDate.now(), "Test entry", "", "", "123", "12345"))
        val transactions = statements.flatMap { it.entries }.toSet()
        submitStatement(oauthService, accessToken, transactions, bankAccountId)
    }

    private fun auth(): Pair<OAuth20Service, OAuth2AccessToken> {
        val build: OAuth20Service = ServiceBuilder(FREEAGENT_API_KEY)
            .apiSecret(FREEAGENT_API_SECRET)
            .callback("http://localhost:8080/auth")
            .build(object : DefaultApi20() {
                override fun getAccessTokenEndpoint(): String {
                    return "https://api.freeagent.com/v2/token_endpoint"
                }

                override fun getAuthorizationBaseUrl(): String {
                    return "https://api.freeagent.com/v2/approve_app"
                }
            })

        val file = File("cred-fa.yaml")
        if (!file.exists()) {
            val authUrl = build.authorizationUrl
            println("Open the url below in a browser, auth with freeagent, then enter the authcode that appears in the callback url")
            println(authUrl)

            latch.await()
            println(authCode)

//            val authCode = readLine().toString()
            println("\'$authCode\'")

            val accessToken = build.getAccessToken(authCode)

            println(accessToken.accessToken)
            println(accessToken.refreshToken)
            println(accessToken.expiresIn)
            println(accessToken.tokenType)

            val expiry = Instant.now().plusSeconds(accessToken.expiresIn.toLong())

            val dump = mapOf(
                "expiry" to expiry.toString(),
                "tokenType" to accessToken.tokenType,
                "accessToken" to accessToken.accessToken,
                "refreshToken" to accessToken.refreshToken
            )

            Yaml().dump(dump, file.writer())

        }

        val auth = Yaml().load<Map<String, String>>(file.reader())
        val expiryInstant = Instant.parse(auth["expiry"])
        if (Instant.now().isAfter(expiryInstant)) {
            val refreshedAccessToken = build.refreshAccessToken(auth["refreshToken"])
            println(refreshedAccessToken.accessToken)
            println(refreshedAccessToken.refreshToken)
            println(refreshedAccessToken.expiresIn)
            println(refreshedAccessToken.tokenType)

            val expiry = Instant.now().plusSeconds(refreshedAccessToken.expiresIn.toLong())

            val dump = mapOf(
                "expiry" to expiry.toString(),
                "tokenType" to refreshedAccessToken.tokenType,
                "accessToken" to refreshedAccessToken.accessToken,
                "refreshToken" to refreshedAccessToken.refreshToken
            )

            Yaml().dump(dump, file.writer())
        }
        val authPostRefresh = Yaml().load<Map<String, String>>(file.reader())
        return build to OAuth2AccessToken(authPostRefresh["accessToken"])
    }

    private fun determineAmount(entry: StatementEntry): String {
        return entry.amountIn.takeIf { it.isNotEmpty() } ?: entry.amountOut.let { "-$it" }
    }

    private fun submitStatement(
        oauthService: OAuth20Service,
        accessToken: OAuth2AccessToken,
        transactions: Set<StatementEntry>,
        bankAccountId: String
    ) {
        val freeAgentEntries =
            transactions.map { FreeAgentStatementEntry(it.date.toString(), determineAmount(it), it.details) }

        val payload = jacksonObjectMapper().writeValueAsString(FreeAgentStatement(freeAgentEntries))

        val request = OAuthRequest(
            Verb.POST,
            "https://api.freeagent.com/v2/bank_transactions/statement?bank_account=$bankAccountId"
        )
        request.headers["Accept"] = "application/json"
        request.headers["Content-Type"] = "application/json"
        request.setPayload(payload)

        oauthService.signRequest(accessToken, request)
        val response = oauthService.execute(request)

        println(response)
        println(response.body)
    }
}

data class FreeAgentStatement(val statement: List<FreeAgentStatementEntry>)
data class FreeAgentStatementEntry(
    @JsonProperty("dated_on") val datedOn: String,
    val amount: String,
    val description: String
)
