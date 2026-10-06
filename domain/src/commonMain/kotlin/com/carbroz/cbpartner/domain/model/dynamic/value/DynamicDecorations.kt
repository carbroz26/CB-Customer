package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicBackground {
    data class Solid(val color: String) : DynamicBackground
    data class LinearGradient(val angle: Double = 0.0, val colors: List<DynamicGradientStop> = emptyList()) : DynamicBackground
}

data class DynamicGradientStop(val color: String)

data class DynamicBorder(val width: Double, val color: String)

sealed interface DynamicShape {
    data class RoundedCorner(val cornerRadius: Double) : DynamicShape
}
