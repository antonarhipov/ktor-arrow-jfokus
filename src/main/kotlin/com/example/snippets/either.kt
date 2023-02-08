package com.example.snippets

import arrow.core.Either


suspend fun main() {

}

fun findUser(): Either<MyError, MyUser> {
    return Either.catch { MyUser() }.mapLeft { MyError() }
}

class MyUser
class MyError
