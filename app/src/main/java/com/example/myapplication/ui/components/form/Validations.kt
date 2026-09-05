package com.example.myapplication.ui.components.form

import java.math.BigDecimal

/**
 * A validation result: either a successfully validated (and possibly
 * type-transformed) value, or an error message.
 *
 * `out T` + `Invalid : Validation<Nothing>` is what lets a chain change
 * types at every step (String -> BigDecimal -> BigDecimal ...) while still
 * short-circuiting cleanly once something fails.
 */
sealed class Validation<out T> {
    data class Valid<T>(val value: T) : Validation<T>()
    data class Invalid(val message: String) : Validation<Nothing>()

    inline fun <R> fold(
        onValid: (T) -> R,
        onInvalid: (String) -> R
    ): R = when (this) {
        is Valid -> onValid(value)
        is Invalid -> onInvalid(message)
    }

    val isValid: Boolean get() = this is Valid
}

/** Entry point: starts a validation chain. */
fun <T> Validate(value: T): Validation<T> = Validation.Valid(value)

/**
 * Transforms the value if Valid, otherwise propagates Invalid.
 * If transform returns null, becomes Invalid(message).
 */
inline fun <T, R> Validation<T>.map(message: String, transform: (T) -> R?): Validation<R> =
    when (this) {
        is Validation.Valid -> transform(value)?.let { Validation.Valid(it) } ?: Validation.Invalid(message)
        is Validation.Invalid -> this
    }

/**
 * Checks a predicate if Valid, otherwise propagates.
 * If predicate is false, becomes Invalid(message).
 */
inline fun <T> Validation<T>.check(message: String, predicate: (T) -> Boolean): Validation<T> =
    when (this) {
        is Validation.Valid -> if (predicate(value)) this else Validation.Invalid(message)
        is Validation.Invalid -> this
    }

fun Validation<String>.isBigDecimal(
    message: String = "must be a valid decimal number"
): Validation<BigDecimal> = map(message) { it.toBigDecimalOrNull() }

@JvmName("isPositiveBigDecimal")
fun Validation<BigDecimal>.isPositive(
    message: String = "must be positive"
): Validation<BigDecimal> = check(message) { it > BigDecimal.ZERO }

fun Validation<String>.isLong(
    message: String = "must be a valid number"
): Validation<Long> = map(message) { it.toLongOrNull() }

@JvmName("isPositiveLong")
fun Validation<Long>.isPositive(
    message: String = "must be positive"
): Validation<Long> = check(message) { it > 0.toLong() }
