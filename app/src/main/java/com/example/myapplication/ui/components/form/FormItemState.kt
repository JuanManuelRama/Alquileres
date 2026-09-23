package com.example.myapplication.ui.components.form

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.ui.components.picker.PickerInputs
import java.time.LocalDate
import java.time.YearMonth

data class Error(
    val isError: Boolean,
    val message: String = "",
    val validationError: ValidationError? = null
) {
    companion object {
        val None: Error = Error(false, "")
        val Simple: Error = Error(true, "")
        fun MessageError(msg: String): Error = Error(true, message = msg, validationError = ValidationError.Text(msg))
        fun ResourceError(@StringRes resId: Int, vararg args: Any): Error = Error(true, validationError = ValidationError.Resource(resId, args.toList()))
    }

    val hasMessage: Boolean = validationError != null || message.isNotBlank()
}

@Composable
fun Error.renderMessage(): String = when (val err = validationError) {
    null -> message
    is ValidationError.Text -> err.message
    is ValidationError.Resource -> stringResource(err.resId, *err.args.toTypedArray())
}

fun Validation<*>.toFormError(): Error = foldError(
    onValid = { Error.None },
    onInvalid = { err ->
        when (err) {
            is ValidationError.Text -> Error.MessageError(err.message)
            is ValidationError.Resource -> Error(true, validationError = err)
        }
    }
)

sealed interface FormItemState {
    val label: String
    val mandatory: Boolean
    val displayLabel: String get() = label + if (mandatory) " *" else ""
    fun error(): Error
}

/** Shared validation policy; field types only define what counts as empty. */
sealed interface ValidatedFormItemState<V> : FormItemState {
    val value: V
    val validate: ((V) -> Validation<*>)?
    val isEmpty: Boolean

    override fun error(): Error = when {
        isEmpty -> if (mandatory) Error.Simple else Error.None
        else -> validate?.invoke(value)?.toFormError() ?: Error.None
    }
}

data class TextFormItemState(
    val field: String,
    override val mandatory: Boolean = false,
    override val value: String,
    val keyboardOptions: KeyboardOptions,
    override val validate: ((String) -> Validation<*>)? = null,
    val onValueChange: (String) -> Unit = {},
    val prefix: (@Composable () -> Unit)? = null,
    val textStyle: TextStyle? = null,
    val showLabel: Boolean = true,
    val modifiable: Boolean = true,
    val visualTransformation: VisualTransformation = VisualTransformation.None
) : ValidatedFormItemState<String> {
    override val label: String get() = this.field
    override val isEmpty: Boolean get() = value.isBlank()
}

data class PickerFormItemState<T>(
    override val label: String,
    override val mandatory: Boolean = false,
    val items: List<T>,
    val selectedItem: T?,
    val onItemSelected: (T?) -> Unit,
    val onItemCreated: (T) -> Unit,
    val itemLabel: (T) -> String,
    val itemSearchText: (T) -> String = itemLabel,
    val createNewLabel: String,
    val createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    val pickerInputs: PickerInputs? = null,
    val onPickerInputsChanged: (PickerInputs) -> Unit = {},
    val placeholder: String? = null,
    override val validate: ((T?) -> Validation<*>)? = null,
) : ValidatedFormItemState<T?> {
    override val value: T? get() = selectedItem
    override val isEmpty: Boolean get() = value == null
}

data class MultiPickerFormItemState<T>(
    override val label: String,
    override val mandatory: Boolean = false,
    val items: List<T>,
    val selectedItems: List<T>,
    val onItemsChanged: (List<T>) -> Unit,
    val onItemCreated: (T) -> Unit,
    val itemLabel: (T) -> String,
    val itemSearchText: (T) -> String = itemLabel,
    val createNewLabel: String,
    val createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    val pickerInputs: PickerInputs? = null,
    val onPickerInputsChanged: (PickerInputs) -> Unit = {},
    val placeholder: String? = null,
    override val validate: ((List<T>) -> Validation<*>)? = null,
) : ValidatedFormItemState<List<T>> {
    override val value: List<T> get() = selectedItems
    override val isEmpty: Boolean get() = value.isEmpty()
}

data class DateFormItemState(
    override val label: String,
    override val mandatory: Boolean = false,
    override val value: LocalDate?,
    val onValueChange: (LocalDate?) -> Unit,
    override val validate: ((LocalDate?) -> Validation<*>)? = null,
) : ValidatedFormItemState<LocalDate?> {
    override val isEmpty: Boolean get() = value == null
}

data class MonthFormItemState(
    override val label: String,
    override val mandatory: Boolean = false,
    override val value: YearMonth?,
    val onValueChange: (YearMonth?) -> Unit,
    override val validate: ((YearMonth?) -> Validation<*>)? = null,
) : ValidatedFormItemState<YearMonth?> {
    override val isEmpty: Boolean get() = value == null
}