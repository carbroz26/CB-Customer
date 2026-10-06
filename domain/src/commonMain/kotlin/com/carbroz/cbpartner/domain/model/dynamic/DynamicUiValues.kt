package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicPadding(
    val start: Double = 0.0,
    val top: Double = 0.0,
    val end: Double = 0.0,
    val bottom: Double = 0.0,
)

sealed interface DynamicArrangement {
    data object Start : DynamicArrangement
    data object Center : DynamicArrangement
    data object End : DynamicArrangement
    data object Top : DynamicArrangement
    data object Bottom : DynamicArrangement
    data object SpaceBetween : DynamicArrangement
    data class SpacedBy(val spacing: Double) : DynamicArrangement
}

sealed interface DynamicBackground {
    data class Solid(val color: String) : DynamicBackground
    data class LinearGradient(
        val angle: Double = 0.0,
        val colors: List<DynamicGradientStop> = emptyList(),
    ) : DynamicBackground
}

data class DynamicGradientStop(
    val color: String,
)

data class DynamicBorder(
    val width: Double,
    val color: String,
)

sealed interface DynamicShape {
    data class RoundedCorner(val cornerRadius: Double) : DynamicShape
}

data class DynamicTextSpan(
    val text: String,
    val color: String? = null,
    val fontWeight: Int? = null,
    val underline: Boolean = false,
    val action: DynamicAction? = null,
)

data class DynamicInputPresentation(
    val type: String,
    val count: Int? = null,
    val spacing: Double? = null,
    val segmentWidth: Double? = null,
    val segmentHeight: Double? = null,
)
