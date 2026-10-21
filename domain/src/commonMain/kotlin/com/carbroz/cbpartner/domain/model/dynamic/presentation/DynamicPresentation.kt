package com.carbroz.cbpartner.domain.model.dynamic.presentation

import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

sealed interface DynamicPresentation {
    val id: String
    val initialBindings: Map<String, DynamicValue>
    val components: List<DynamicComponent>
}

data class DialogPresentation(
    override val id: String,
    val dismissOnOutsideClick: Boolean? = null,
    val dismissOnBack: Boolean? = null,
    override val initialBindings: Map<String, DynamicValue> = emptyMap(),
    override val components: List<DynamicComponent> = emptyList(),
) : DynamicPresentation

data class BottomSheetPresentation(
    override val id: String,
    val dismissOnOutsideClick: Boolean? = null,
    val dismissOnBack: Boolean? = null,
    val showHandle: Boolean? = null,
    val handle: DynamicPresentationHandle? = null,
    override val initialBindings: Map<String, DynamicValue> = emptyMap(),
    override val components: List<DynamicComponent> = emptyList(),
) : DynamicPresentation

data class DynamicPresentationHandle(
    val width: Double? = null,
    val height: Double? = null,
    val color: String? = null,
    val cornerRadius: Double? = null,
    val alignment: String? = null,
    val spacing: DynamicSpacing? = null,
)
