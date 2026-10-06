package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicShape {
    data class RoundedCorner(val cornerRadius: Double) : DynamicShape
}
