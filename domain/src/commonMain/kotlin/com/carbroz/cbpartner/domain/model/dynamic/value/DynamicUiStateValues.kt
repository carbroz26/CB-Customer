package com.carbroz.cbpartner.domain.model.dynamic.value

data class DynamicSelectedStyle(
    val background: DynamicBackground? = null,
)

data class DynamicFocusPresentation(
    val binding: String,
    val border: DynamicBorder,
)

data class DynamicValidationPresentation(
    val binding: String,
    val errorBorder: DynamicBorder,
    val messageStyle: DynamicValidationMessageStyle? = null,
)

data class DynamicValidationMessageStyle(
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val color: String? = null,
    val spacing: DynamicSpacing? = null,
    val reserveHeight: Double? = null,
)

data class DynamicValidationStyle(
    val background: DynamicBackground? = null,
)

data class DynamicButtonLoading(
    val whileActionPending: Boolean,
    val disableWhilePending: Boolean,
    val replaceContent: Boolean,
    val indicator: DynamicLoadingIndicator,
)

sealed interface DynamicLoadingIndicator {
    data class Circular(
        val size: Double,
        val color: String,
    ) : DynamicLoadingIndicator
}
