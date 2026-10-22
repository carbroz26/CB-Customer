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
    val errorBorder: DynamicBorder? = null,
    val messageStyle: DynamicValidationMessageStyle? = null,
)

data class DynamicValidationMessageStyle(
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val color: String? = null,
    val spacing: DynamicSpacing? = null,
    val reserveSpaceWhenHidden: Boolean = false,
    val minHeight: Double? = null,
)

data class DynamicValidationStyle(
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
)

data class DynamicButtonEnabledWhen(
    val binding: String,
    val length: Int,
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
