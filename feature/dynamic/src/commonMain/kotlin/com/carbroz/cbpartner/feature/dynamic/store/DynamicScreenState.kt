package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/** Transient identity of exactly one pending Button in its current UI scope. */
data class DynamicPendingButton(
    val id: String,
    val presentationId: String? = null,
    val presentationGeneration: Long? = null,
)

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
    val presentationGeneration: Long = 0L,
    val pendingButton: DynamicPendingButton? = null,
    val transientState: Map<String, DynamicValue> = emptyMap(),
)
