package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicBackground {
    data class Solid(val color: String) : DynamicBackground
    data class LinearGradient(
        val angle: Double = 0.0,
        val colors: List<DynamicGradientStop> = emptyList(),
    ) : DynamicBackground
}
