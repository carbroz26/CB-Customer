package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.presentation.DynamicPresentationDto
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentationHandle
import com.carbroz.cbpartner.domain.model.dynamic.presentation.ToastPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.ToastProgress
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
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
                val heightFraction = values.number(dto.properties, "heightFraction")
                require(heightFraction == null ||
                    (heightFraction.isFinite() && heightFraction > 0.0 && heightFraction <= 1.0)) {
                    "Bottom sheet heightFraction must be within (0, 1]"
                }
                BottomSheetPresentation(
                    id = dto.id,
                    dismissOnOutsideClick = values.boolean(dto.properties, "dismissOnOutsideClick"),
                    dismissOnBack = values.boolean(dto.properties, "dismissOnBack"),
                    showHandle = showHandle,
                    handle = handle,
                    heightFraction = heightFraction,
                    background = values.background(dto.properties),
                    shape = values.shape(dto.properties),
                    initialBindings = values.toDynamicValueMap(dto.initialBindings),
                    components = mappedComponents,
                )
            }
            "toast" -> {
                require(dto.properties.keys == setOf("placement", "durationMillis", "background",
                    "shape", "padding", "spacing", "progress") ||
                    dto.properties.keys == setOf("placement", "durationMillis", "background",
                        "shape", "padding", "progress")) {
                    "Toast properties must be explicitly authored with supported keys"
                }
                require(values.string(dto.properties, "placement") == "top") {
                    "Toast V1 only supports top placement"
                }
                val durationValue = (dto.properties["durationMillis"] as? JsonPrimitive)
                    ?.takeUnless { it.isString }?.doubleOrNull
                    ?: error("Toast durationMillis must be a number")
                require(durationValue.isFinite() && durationValue in 500.0..15000.0 &&
                    durationValue % 1.0 == 0.0) { "Invalid Toast durationMillis" }
                val background = requireNotNull(values.background(dto.properties)) {
                    "Toast background is required"
                }
                val shape = requireNotNull(values.shape(dto.properties)) {
                    "Toast rounded shape is required"
                }
                val padding = requireNotNull(values.padding(dto.properties)) {
                    "Toast content padding is required"
                }
                require(mappedComponents.size == 1 && mappedComponents.single() is StackComponent) {
                    "Toast V1 requires one existing StackComponent"
                }
                val progressObject = dto.properties["progress"] as? JsonObject
                    ?: error("Toast progress is required")
                require(progressObject.keys.all { it in setOf("height", "trackColor", "fillColor", "padding") } &&
                    progressObject.keys.containsAll(setOf("height", "trackColor", "fillColor"))) {
                    "Toast progress keys are invalid"
                }
                val height = values.number(progressObject, "height")
                    ?: error("Toast progress.height is required")
                require(height.isFinite() && height > 0 && height <= 12) {
                    "Invalid Toast progress height"
                }
                val track = values.string(progressObject, "trackColor")
                    ?: error("Toast trackColor is required")
                val fill = values.string(progressObject, "fillColor")
                    ?: error("Toast fillColor is required")
                val hexColor = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")
                require(hexColor.matches(track) && hexColor.matches(fill)) {
                    "Toast progress colors must be valid hex"
                }
                ToastPresentation(
                    id = dto.id,
                    placement = "top",
                    durationMillis = durationValue.toLong(),
                    background = background,
                    shape = shape,
                    padding = padding,
                    spacing = values.spacing(dto.properties),
                    progress = ToastProgress(height, track, fill, values.padding(progressObject)),
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
