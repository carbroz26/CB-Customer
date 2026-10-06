package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicArrangement {
    data object Start : DynamicArrangement
    data object Center : DynamicArrangement
    data object End : DynamicArrangement
    data object Top : DynamicArrangement
    data object Bottom : DynamicArrangement
    data object SpaceBetween : DynamicArrangement
    data class SpacedBy(val spacing: Double) : DynamicArrangement
}
