package com.example.myapplication.ui.components.form

data class TypeaheadOption(
    val value: String,
    val autoFill: Map<FormFieldKey, String> = emptyMap()
)

data class TypeaheadItemState(
    override val key: FormFieldKey,
    override val field: String,
    override val mandatory: Boolean = false,
    val value: String,
    val options: List<TypeaheadOption>,
    val validate: ((String) -> Boolean)? = null,
    val onCreateNew: ((String) -> Unit)? = null,
    override val onValueChange: (String) -> Unit,
    val onOptionSelected: (TypeaheadOption) -> Unit
) : FormFieldState {
    override fun isValidated(): Boolean =
        (!mandatory || value.isNotBlank()) && (validate?.invoke(value) ?: true)
}