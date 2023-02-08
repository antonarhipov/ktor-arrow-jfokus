package com.example

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import arrow.fx.coroutines.CircuitBreaker
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.continuations.resource
import com.example.external.*
import com.example.external.env.Env
import com.example.external.impl.BillingImpl
import com.example.external.impl.WarehouseImpl
import com.example.plugins.*
import com.example.validation.validateStructure
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

suspend fun main() {
    val env = Env()
    dependencies(env).use { module: ExampleApp ->
        embeddedServer(Netty, host = env.http.host, port = env.http.port) {
            module.configure(this)
        }.start(wait = true)
    }
}

// dependencies are declared as interfaces
// where we mark everything as suspend
class ExampleApp(
    private val warehouse: Warehouse,
    private val billing: Billing,
) {
    fun configure(app: Application) = app.run {
        install(ContentNegotiation) { gson() }
        install(AutoHeadResponse)

        routing {
            get("/hello") {
                call.respondText("Hello World!")
            }

            get("/process") {
                val result = either<BadRequest, List<Entry>> {
                    val order = Either.catch { call.receive<Order>() }
                        .mapLeft { badRequest(it.message ?: "Received an invalid order") }
                        .bind()

                    listOf(1, 2, 3).map {
                        it.right().bind()
                    }

                    validateStructure(order).mapLeft { problems ->
                        badRequest(problems.joinToString { it.name })
                    }.bind()

                    order.entries.parTraverseValidated {
                        warehouse.validateAvailability(it.id, it.amount)
                    }.mapLeft { availability ->
                        badRequest("Following productIds weren't available: ${availability.joinToString { it.productId.value }}")
                    }.bind()
                }
                when (result) {
                    is Either.Left<BadRequest> ->
                        call.respond(result.value)

                    is Either.Right<List<Entry>> ->
                        when (billing.processBilling(result.value.associate(Entry::asPair))) {
                            BillingResponse.OK ->
                                call.respondText("ok")

                            BillingResponse.USER_ERROR ->
                                call.respond(badRequest("not enough items"))

                            BillingResponse.SYSTEM_ERROR ->
                                call.respondText(status = HttpStatusCode.InternalServerError) { "server error" }
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun dependencies(env: Env): Resource<ExampleApp> = resource {
    val circuitBreaker = CircuitBreaker.of(
        maxFailures = 2,
        resetTimeout = 2.seconds,
        exponentialBackoffFactor = 2.0,           // enable exponentialBackoffFactor
        maxResetTimeout = 60.seconds,             // limit exponential back-off time
    )
    val retries = 5
    ExampleApp(
        WarehouseImpl(Url("my.internal.warehouse.service")),
        BillingImpl(Url("my.external.billing.service")).withBreaker(circuitBreaker, retries)
    )
}

typealias BadRequest = TextContent

private fun badRequest(message: String): TextContent =
    TextContent(message, ContentType.Text.Plain, HttpStatusCode.BadRequest)
