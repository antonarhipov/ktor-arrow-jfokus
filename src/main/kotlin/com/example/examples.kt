package com.example

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right


suspend fun main() {

}

fun findUser(): Either<MyError, MyUser> {
    return Either.catch { MyUser() }.mapLeft { MyError() }
}

class MyUser
class MyError
