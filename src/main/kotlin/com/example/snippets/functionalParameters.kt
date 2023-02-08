package com.example.snippets

import kotlinx.coroutines.runBlocking

//region domain
class A {
    suspend fun close() {}
}

class B {
    suspend fun close() {}
}

class C {
    suspend fun close() {}
}
//endregion


class PseudoClosableExample {
    lateinit var service: A
    lateinit var broker: B
    lateinit var producer: C

    fun appCleanup() {
        stopProducer()
        stopBroker()
        stopService()
    }

    //region Duplicates
    fun stopProducer() {
        if (this::producer.isInitialized) {
            runBlocking {
                runCatching {
                    producer.close()
                }.onFailure {
                    println("failed to close queue producer: $it")
                }
            }
        }
    }

    fun stopBroker() {
        if (this::broker.isInitialized) {
            runBlocking {
                runCatching {
                    broker.close()
                }.onFailure { println("failed to close queue producer: $it") }
            }
        }
    }

    fun stopService() {
        if (this::service.isInitialized) {
            runBlocking {
                runCatching {
                    service.close()
                }.onFailure { println("failed to close queue producer: $it") }
            }
        }
    }
    //endregion
}

class PseudoClosableExample2 {

    lateinit var service: A
    lateinit var broker: B
    lateinit var producer: C

    fun appCleanup() {
        stopProducer()
        stopBroker()
        stopService()
    }

    //region reflection & when
    fun stopProducer() {
        cleanup(this::producer.isInitialized, producer)
    }

    fun stopBroker() {
        cleanup(this::broker.isInitialized, broker)
    }

    fun stopService() {
        cleanup(this::service.isInitialized, service)
    }

    fun cleanup(isInitialized: Boolean, closeable: Any) {
        if (isInitialized) {
            runBlocking {
                val className = closeable::class.java.simpleName
                runCatching {
                    when (closeable) {
                        is A -> closeable.close()
                        is B -> closeable.close()
                        is C -> closeable.close()
                        else -> {
                            throw IllegalArgumentException("unsupported class $className")
                        }
                    }
                }.onFailure { println("failed to close $className: $it") }
            }
        }
    }
    //endregion
}


class PseudoClosableFunExample {
    lateinit var service: A
    lateinit var broker: B
    lateinit var producer: C

    fun appCleanup() {
        stopResrouce({ this::producer.isInitialized }) { producer.close() }
        stopResrouce({ this::broker.isInitialized }) { broker.close() }
        stopResrouce({ this::service.isInitialized }) { service.close() }
    }

    //region suspendable lambda
    fun stopResrouce(predicate: () -> Boolean, close: suspend () -> Unit) {
        if (predicate()) {
            runBlocking {
                runCatching {
                    close()
                }.onFailure {
                    println("failed to close the resource: $it")
                }
            }
        }
    }
    //endregion
}