package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicScreenState(
    val response: DynamicResponse? = null,
    val bindings: Map<String, DynamicValue> = emptyMap(),
    val validationMessage: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val focusedBinding: String? = null,
    val activePresentationId: String? = null,
    val presentationBindings: Map<String, DynamicValue> = emptyMap(),
    val presentationValidationErrors: Map<String, String> = emptyMap(),
    val presentationFocusedBinding: String? = null,
)
