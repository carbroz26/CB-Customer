package com.carbroz.cbpartner.domain.model.dynamic.value

sealed interface DynamicAssetSource {
    val value: DynamicValue

    data class Url(
        override val value: DynamicValue,
    ) : DynamicAssetSource

    data class Resource(
        override val value: DynamicValue,
    ) : DynamicAssetSource
}
