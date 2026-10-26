package com.carbroz.cbpartner.domain.model.error

import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation

/**
 * A typed, UI-independent operation failure; no transport exception text is exposed.
 * The optional presentation is a validated immediate backend response, not a screen entry.
 */
data class AppFailure(
    val category: Category,
    val responseCode: String? = null,
    val userMessage: String? = null,
    val presentation: DialogPresentation? = null,
    val httpStatus: Int? = null,
) : Exception("Application request failed: " + category.name) {
    enum class Category {
        CONNECTION, SERVER, SECURE_CONNECTION, ACCESS, VALIDATION, RATE_LIMIT, UNKNOWN,
    }
}

