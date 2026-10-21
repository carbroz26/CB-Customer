package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

/**
 * Standalone, backend-sized Divider Element. Different from accessory DividerContent.
 * Both axes and styling are explicit; invalid instances fail before rendering.
 */
data class DividerElement(
    val id: String,
    val orientation: String,
    val width: Double? = null,
    val height: Double? = null,
    val thickness: Double,
    val color: String,
    override val spacing: DynamicSpacing? = null,
    val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement {
    init {
        require(orientation == "horizontal" || orientation == "vertical") {
            "Dynamic divider orientation must be horizontal or vertical"
        }
        require(thickness.isFinite() && thickness > 0.0) {
            "Dynamic divider thickness must be finite and positive"
        }
        when (orientation) {
            "horizontal" -> {
                require(height == null && width != null && width.isFinite() && width > 0.0) {
                    "Horizontal dynamic divider requires positive finite width and no height"
                }
            }
            "vertical" -> {
                require(width == null && height != null && height.isFinite() && height > 0.0) {
                    "Vertical dynamic divider requires positive finite height and no width"
                }
            }
        }
        require(color.matches(Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$"))) {
            "Dynamic divider color must be a supported hex value"
        }
        require(spacing == null || listOf(
            spacing.start, spacing.top, spacing.end, spacing.bottom,
        ).all { it.isFinite() && it >= 0.0 }) {
            "Dynamic divider spacing must be finite and nonnegative"
        }
    }
}
