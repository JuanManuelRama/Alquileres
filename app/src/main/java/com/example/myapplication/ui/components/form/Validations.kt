package com.example.myapplication.ui.components.form

import androidx.annotation.StringRes
import com.example.myapplication.R
import java.math.BigDecimal

sealed interface ValidationError {
    data class Text(val message: String) : ValidationError
    data class Resource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : ValidationError
}

/**
 * A validation result: either a successfully validated (and possibly
 * type-transformed) value, or a validation error.
 */
sealed class Validation<out T> {
    data class Valid<T>(val value: T) : Validation<T>()
    data class Invalid(val error: ValidationError) : Validation<Nothing>() {
        constructor(message: String) : this(ValidationError.Text(message))
        constructor(@StringRes resId: Int, vararg args: Any) : this(ValidationError.Resource(resId, args.toList()))
    }

    inline fun <R> fold(
        onValid: (T) -> R,
        onInvalid: (String) -> R
    ): R = when (this) {
        is Valid -> onValid(value)
        is Invalid -> onInvalid(when (error) {
            is ValidationError.Text -> error.message
            is ValidationError.Resource -> error.resId.toString()
        })
    }

    inline fun <R> foldError(
        onValid: (T) -> R,
        onInvalid: (ValidationError) -> R
    ): R = when (this) {
        is Valid -> onValid(value)
        is Invalid -> onInvalid(error)
    }

    val isValid: Boolean get() = this is Valid
}

/** Entry point: starts a validation chain. */
fun <T> Validate(value: T): Validation<T> = Validation.Valid(value)

/** Transforms the value if Valid, otherwise propagates Invalid. */
inline fun <T, R> Validation<T>.map(
    @StringRes messageRes: Int,
    transform: (T) -> R?
): Validation<R> = when (this) {
    is Validation.Valid -> transform(value)?.let { Validation.Valid(it) } ?: Validation.Invalid(messageRes)
    is Validation.Invalid -> this
}

inline fun <T, R> Validation<T>.map(
    message: String,
    transform: (T) -> R?
): Validation<R> = when (this) {
    is Validation.Valid -> transform(value)?.let { Validation.Valid(it) } ?: Validation.Invalid(message)
    is Validation.Invalid -> this
}

/** Checks a predicate if Valid, otherwise propagates Invalid. */
inline fun <T> Validation<T>.check(
    @StringRes messageRes: Int,
    vararg args: Any,
    predicate: (T) -> Boolean
): Validation<T> = when (this) {
    is Validation.Valid -> if (predicate(value)) this else Validation.Invalid(messageRes, *args)
    is Validation.Invalid -> this
}

inline fun <T> Validation<T>.check(
    message: String,
    predicate: (T) -> Boolean
): Validation<T> = when (this) {
    is Validation.Valid -> if (predicate(value)) this else Validation.Invalid(message)
    is Validation.Invalid -> this
}

fun Validation<String>.isBigDecimal(
    @StringRes messageRes: Int = R.string.validation_invalid_decimal
): Validation<BigDecimal> = map(messageRes) { it.toBigDecimalOrNull() }

fun Validation<String>.isLong(
    @StringRes messageRes: Int = R.string.validation_invalid_number
): Validation<Long> = map(messageRes) { it.toLongOrNull() }

@JvmName("isPositiveBigDecimal")
fun Validation<BigDecimal>.isPositive(
    @StringRes messageRes: Int = R.string.validation_must_be_positive
): Validation<BigDecimal> = check(messageRes) { it > BigDecimal.ZERO }

@JvmName("isPositiveLong")
fun Validation<Long>.isPositive(
    @StringRes messageRes: Int = R.string.validation_must_be_positive
): Validation<Long> = check(messageRes) { it > 0.toLong() }
fun Validation<BigDecimal>.limitDecimals(
    decimals: Int,
    @StringRes messageRes: Int = R.string.validation_max_decimals
): Validation<BigDecimal> = check(messageRes, decimals) { it.scale() <= decimals }

fun <T> Validation<T>.doesntExist(
    elements: Set<T>,
    @StringRes messageRes: Int = R.string.validation_must_be_unique
): Validation<T> = check(messageRes) { it !in elements }

fun Validation<String>.maxLen(
    length: Int,
    @StringRes messageRes: Int = R.string.validation_max_length
): Validation<String> = check(messageRes, length) { it.length <= length }


fun Validation<String>.noInvalidChars(
    @StringRes messageRes: Int = R.string.validation_invalid_characters
): Validation<String> = check(messageRes) { it.none { char ->
    char.isISOControl() || char == '\u200B' || char == '\uFEFF' }
}