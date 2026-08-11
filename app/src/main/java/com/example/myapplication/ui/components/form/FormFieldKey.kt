// FormFieldKey.kt
package com.example.myapplication.ui.components.form

/**
 * Identity-based key for a form field. Two keys are only equal if they're
 * the *same instance* — deliberately not a data class, so equality is
 * reference equality and there's no way to "typo" a match.
 */
class FormFieldKey(val label: String)