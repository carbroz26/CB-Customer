package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.presentation.DynamicPresentationDto
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentationHandle
import kotlinx.serialization.json.JsonObject

class PresentationMapper(
    private val components: ComponentMapper,
    private val values: DynamicValueMapper,
) {
    fun map(dto: DynamicPresentationDto): DynamicPresentation {
        require(dto.id.isNotBlank()) { "Dynamic presentation id is required" }
        val mappedComponents = dto.components.map(components::map)
        require(mappedComponents.isNotEmpty()) { "Dynamic presentation components are required" }

        return when (dto.type) {
            "dialog" -> DialogPresentation(
                id = dto.id,
                dismissOnOutsideClick = values.boolean(dto.properties, "dismissOnOutsideClick"),
                dismissOnBack = values.boolean(dto.properties, "dismissOnBack"),
                initialBindings = values.toDynamicValueMap(dto.initialBindings),
                components = mappedComponents,
            )
            "bottom_sheet" -> {
                val showHandle = values.boolean(dto.properties, "showHandle")
                val handle = (dto.properties["handle"] as? JsonObject)?.let(::mapHandle)
                if (showHandle == true) {
                    requireNotNull(handle) { "Dynamic bottom_sheet handle is required when showHandle is true" }
                }
                BottomSheetPresentation(
                    id = dto.id,
                    dismissOnOutsideClick = values.boolean(dto.properties, "dismissOnOutsideClick"),
                    dismissOnBack = values.boolean(dto.properties, "dismissOnBack"),
                    showHandle = showHandle,
                    handle = handle,
                    initialBindings = values.toDynamicValueMap(dto.initialBindings),
                    components = mappedComponents,
                )
            }
            else -> throw IllegalArgumentException("Unsupported dynamic presentation type: " + dto.type)
        }
    }

    private fun mapHandle(value: JsonObject) = DynamicPresentationHandle(
        width = values.number(value, "width"),
        height = values.number(value, "height"),
        color = values.string(value, "color"),
        cornerRadius = values.number(value, "cornerRadius"),
        alignment = values.string(value, "alignment"),
        spacing = values.spacing(value),
    )
}
