package com.mrd.test

import kotlin.random.Random

fun randomString(n: Int): String {
    require(n <= 100) { "n must be <= 100" }
    return buildString {
        repeat(n) {
            append(Random.nextInt('a'.code, 'z'.code + 1).toChar())
        }
    }
}

infix fun <A : Any> List<A>.shouldContainAny(predicate: (A) -> Boolean): List<A> {
    val result = this.any(predicate)
    require(result) { "List should contain at least one element that matches the predicate" }
    return this
}

infix fun <A : Any> List<A>.shouldContainAll(predicate: (A) -> Boolean): List<A> {
    val result = this.all(predicate)
    require(result) { "List should contain at least one element that matches the predicate" }
    return this
}


