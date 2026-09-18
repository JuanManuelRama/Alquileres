package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.R
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber.PhoneNumber

/** Keep a leading + while editing so international numbers can be parsed correctly. */
internal fun normalizePhoneInput(value: String): String = buildString {
    value.trim().forEach { char ->
        when {
            char == '+' && isEmpty() -> append(char)
            char.isDigit() -> append(char.digitToInt())
        }
    }
}

/** Storage remains a plain digit string, including any explicitly entered country code. */
fun phoneNumberDigits(value: String): String = normalizePhoneInput(value).filter { it in '0'..'9' }

private fun validPhoneNumber(value: String, region: String, util: PhoneNumberUtil): PhoneNumber? =
    try {
        util.parse(value, region).takeIf(util::isValidNumber)
    } catch (_: NumberParseException) {
        null
    }

/** Infer a missing + only for a complete valid number; prefer local numbers when ambiguous. */
internal fun phoneNumberForParsing(value: String, region: String, util: PhoneNumberUtil): String {
    val input = normalizePhoneInput(value)
    if (input.isEmpty() || input.startsWith('+')) return input
    val local = validPhoneNumber(input, region, util)
    val international = validPhoneNumber("+$input", "ZZ", util) ?: return input
    // Some regions already accept their own country code without + when parsing.
    return if (local == null || local == international) "+$input" else input
}

internal fun validatePhoneNumber(
    value: String,
    region: String,
    util: PhoneNumberUtil,
    errorMessage: String = "Enter a valid phone number"
): Validation<String> {
    val number = phoneNumberForParsing(value, region, util)
    return if (validPhoneNumber(number, region, util) != null) Validation.Valid(phoneNumberDigits(value))
    else Validation.Invalid(errorMessage)
}

@Composable
fun FormScope.PhoneItem(
    field: String,
    value: String,
    mandatory: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    region: String = LocalLocale.current.platformLocale.country.ifBlank { "ZZ" },
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val util = remember(context) { PhoneNumberUtil.createInstance(context) }
    val transformation = remember(util, region) { PhoneVisualTransformation(util, region) }
    val invalidPhoneError = stringResource(R.string.err_invalid_phone_number)
    Item(
        field = field,
        value = normalizePhoneInput(value),
        mandatory = mandatory,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        validate = { validatePhoneNumber(it, region, util, invalidPhoneError) },
        visualTransformation = transformation,
        modifier = modifier,
        onValueChange = { onValueChange(normalizePhoneInput(it)) }
    )
}

/** Formatting stays out of the editable value, so deleting separators never traps the cursor. */
internal class PhoneVisualTransformation(
    private val util: PhoneNumberUtil,
    private val region: String
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatter = util.getAsYouTypeFormatter(region)
        val number = phoneNumberForParsing(text.text, region, util)
        val inferredPrefix = number.startsWith('+') && !text.text.startsWith('+')
        var formatted = ""
        number.forEach { formatted = formatter.inputDigit(it) }
        val positions = formatted.indices.filter {
            formatted[it].isDigit() || formatted[it] == '+' && !inferredPrefix
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset == 0 -> positions.firstOrNull() ?: 0
                offset >= text.length -> formatted.length
                else -> positions[offset - 1] + 1
            }

            override fun transformedToOriginal(offset: Int): Int =
                positions.count { it < offset }.coerceAtMost(text.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
