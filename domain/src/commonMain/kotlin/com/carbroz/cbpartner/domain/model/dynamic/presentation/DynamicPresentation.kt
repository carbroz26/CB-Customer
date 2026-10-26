package com.carbroz.cbpartner.domain.model.dynamic.presentation

import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
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
    val heightFraction: Double? = null,
    val background: DynamicBackground? = null,
    val shape: DynamicShape? = null,
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

/** Transient top-of-app presentation; visual content still uses normal registered Components. */
data class ToastPresentation(
    override val id: String,
    val placement: String,
    val durationMillis: Long,
    val background: DynamicBackground,
    val shape: DynamicShape,
    val padding: DynamicPadding,
    val spacing: DynamicSpacing? = null,
    val progress: ToastProgress,
    override val initialBindings: Map<String, DynamicValue> = emptyMap(),
    override val components: List<DynamicComponent> = emptyList(),
) : DynamicPresentation

/** Toast's timed bottom track; this is presentation chrome, never an Element. */
data class ToastProgress(
    val height: Double,
    val trackColor: String,
    val fillColor: String,
    val padding: DynamicPadding? = null,
)
