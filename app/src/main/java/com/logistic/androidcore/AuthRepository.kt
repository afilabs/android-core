package com.logistic.androidcore

interface AuthRepository {
    val name: String
}

class AuthRepository1 : AuthRepository {
    override val name: String
        get() = "Auth 1"

}

class AuthRepository2 : AuthRepository {
    override val name: String
        get() = "Auth 2"
}