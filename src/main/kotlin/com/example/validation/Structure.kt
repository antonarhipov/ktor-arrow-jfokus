package com.example.validation

import arrow.core.*
import arrow.core.continuations.either
import arrow.core.continuations.either.eager
import com.example.*

enum class ValidateStructureProblem {
    EMPTY_ORDER,
    EMPTY_ID,
    INCORRECT_ID,
    NON_POSITIVE_AMOUNT
}

suspend fun validateStructure(order: Order): ValidatedNel<ValidateStructureProblem, Order> =
    either<Nel<ValidateStructureProblem>, Order> {
        ensure(order.entries.isNotEmpty()) { ValidateStructureProblem.EMPTY_ORDER.nel() }
        order.entries.traverse(::validateEntry).bind()
        order.flatten()
    }.toValidated()

fun validateEntry(entry: Entry): ValidatedNel<ValidateStructureProblem, Entry> =
    validateEntryId(entry.id).zip(validateEntryAmount(entry.amount), ::Entry)

fun validateEntryId(id: ProductId): ValidatedNel<ValidateStructureProblem, ProductId> =
    eager<Nel<ValidateStructureProblem>, ProductId> {
        ensure(id.value.isNotEmpty()) { ValidateStructureProblem.EMPTY_ID.nel() }
        ensure(Regex("^ID-(\\d){4}\$").matches(id.value)) { ValidateStructureProblem.INCORRECT_ID.nel() }
        id
    }.toValidated()

fun validateEntryAmount(amount: Int): ValidatedNel<ValidateStructureProblem, Int> =
    amount.ensure({ it > 0 }) { ValidateStructureProblem.NON_POSITIVE_AMOUNT }