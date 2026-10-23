package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.section.FormFieldSectionDto
import com.carbroz.cbpartner.data.dynamic.model.section.ListSectionDto
import com.carbroz.cbpartner.data.dynamic.model.section.StackSectionDto
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListFilter
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSelection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSeparator
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldControlSurface
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicFocusPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationMessageStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SectionMapper(
    private val json: Json,
    private val elements: ElementMapper,
    private val groups: GroupMapper,
    private val values: DynamicValueMapper,
) {
    fun map(node: JsonObject): DynamicSection = when (values.string(node, "type")) {
        "stack_section" -> map(json.decodeFromJsonElement(StackSectionDto.serializer(), node))
        "form_field_section" -> map(json.decodeFromJsonElement(FormFieldSectionDto.serializer(), node))
        "list_section" -> {
            require("emptyGroup" !in node) { "Legacy ListSection emptyGroup is not supported" }
            require("emptySectionId" !in node) { "ListSection emptySectionId belongs in properties" }
            map(json.decodeFromJsonElement(ListSectionDto.serializer(), node))
        }
        else -> throw IllegalArgumentException("Unsupported dynamic section type: " + values.string(node, "type"))
    }

    private fun map(dto: StackSectionDto): StackSection {
        require(dto.type == "stack_section") { "Unsupported dynamic section type: " + dto.type }
        return StackSection(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalArrangement = values.horizontalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            verticalAlignment = values.verticalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            elements = dto.elements.map(elements::map),
            groups = dto.groups.map(groups::map),
        )
    }


    /** Strictly map a reusable field with one labelled, uniquely bound control. */
    private fun map(dto: FormFieldSectionDto): FormFieldSection {
        require(dto.type == "form_field_section") { "Unsupported FormFieldSection type" }
        require(dto.elements.isNotEmpty()) { "FormFieldSection needs a label" }
        val mapped = dto.elements.map(elements::map)
        val label = mapped.firstOrNull() as? TextElement
            ?: error("FormFieldSection first element must be a TextElement label")
        val childGroup: StackGroup?
        val control: InputElement?
        when {
            dto.groups.isEmpty() -> {
                require(mapped.size == 2) { "Direct FormFieldSection requires exactly label and input" }
                control = mapped[1] as? InputElement
                    ?: error("Direct FormFieldSection control must be InputElement")
                childGroup = null
            }
            dto.groups.size == 1 -> {
                require(mapped.size == 1) { "Composite FormFieldSection requires only one label element" }
                childGroup = groups.map(dto.groups.single()) as? StackGroup
                    ?: error("Composite FormFieldSection control must be StackGroup")
                require(childGroup.elements.filterIsInstance<InputElement>().size == 1) {
                    "Composite FormFieldSection requires exactly one input"
                }
                control = null
            }
            else -> error("FormFieldSection accepts at most one control Group")
        }
        val input = control ?: requireNotNull(childGroup)
            .elements.filterIsInstance<InputElement>().single()
        val key = input.binding?.key?.takeIf(String::isNotBlank)
            ?: error("FormFieldSection input must declare a nonblank binding")
        require(input.border == null && input.focusBorder == null) {
            "FormFieldSection input cannot own a competing outer border"
        }
        if (childGroup != null) {
            val properties = dto.groups.single()["properties"] as? JsonObject ?: JsonObject(emptyMap())
            require(setOf("height", "background", "border", "shape", "focusPresentation",
                "validationPresentation").none { it in properties }) {
                "FormFieldSection control Group cannot own outer surface or field state"
            }
        }

        val props = dto.properties
        require(props.keys.all { it in setOf("fillMaxWidth", "horizontalAlignment", "padding",
            "controlSurface", "focusPresentation", "validationPresentation") }) {
            "Unsupported FormFieldSection property"
        }
        val alignment = values.string(props, "horizontalAlignment")
        require(alignment in setOf("start", "center", "end")) {
            "FormFieldSection horizontalAlignment must be start, center, or end"
        }
        val fill = values.boolean(props, "fillMaxWidth")
            ?: error("FormFieldSection fillMaxWidth must be authored")
        val surface = props["controlSurface"] as? JsonObject
            ?: error("FormFieldSection controlSurface is required")
        require(surface.keys.all { it in setOf("height", "fillMaxWidth",
            "background", "border", "shape") }) {
            "Unsupported FormFieldSection controlSurface property"
        }
        val height = values.number(surface, "height")
            ?: error("FormFieldSection controlSurface height is required")
        val width = values.boolean(surface, "fillMaxWidth")
            ?: error("FormFieldSection controlSurface fillMaxWidth is required")
        val controlSurface = FormFieldControlSurface(
            height = height,
            fillMaxWidth = width,
            background = values.background(surface),
            border = values.border(surface),
            shape = values.shape(surface),
        )
        val focus = (props["focusPresentation"] as? JsonObject)?.let { raw ->
            require(raw.keys == setOf("binding", "border")) {
                "FormFieldSection focusPresentation keys are invalid"
            }
            DynamicFocusPresentation(
                binding = values.string(raw, "binding")?.takeIf(String::isNotBlank)
                    ?: error("FormFieldSection focus binding is required"),
                border = values.border(raw) ?: error("FormFieldSection focus border is required"),
            )
        }
        val validation = (props["validationPresentation"] as? JsonObject)?.let { raw ->
            require(raw.keys == setOf("binding", "errorBorder", "messageStyle")) {
                "FormFieldSection validationPresentation keys are invalid"
            }
            val style = raw["messageStyle"] as? JsonObject
                ?: error("FormFieldSection messageStyle is required")
            require(style.keys.all { it in setOf("fontSize", "fontWeight", "color",
                "spacing", "reserveHeight") }) {
                "Unsupported FormFieldSection messageStyle property"
            }
            val reserveHeight = values.number(style, "reserveHeight")
                ?: error("FormFieldSection reserveHeight must be numeric")
            require(reserveHeight.isFinite() && reserveHeight > 0.0) {
                "FormFieldSection reserveHeight must be positive and finite"
            }
            DynamicValidationPresentation(
                binding = values.string(raw, "binding")?.takeIf(String::isNotBlank)
                    ?: error("FormFieldSection validation binding is required"),
                errorBorder = values.border(raw, "errorBorder")
                    ?: error("FormFieldSection errorBorder is required"),
                messageStyle = DynamicValidationMessageStyle(
                    fontSize = values.number(style, "fontSize"),
                    fontWeight = values.number(style, "fontWeight")?.toInt(),
                    color = values.string(style, "color"),
                    spacing = values.spacing(style),
                    reserveHeight = reserveHeight,
                ),
            )
        }
        return FormFieldSection(
            id = dto.id,
            label = label,
            controlInput = control,
            controlGroup = childGroup,
            inputBinding = key,
            controlSurface = controlSurface,
            horizontalAlignment = values.horizontalAlignment(props),
            fillMaxWidth = fill,
            padding = values.padding(props),
            focusPresentation = focus,
            validationPresentation = validation,
        )
    }

    private fun map(dto: ListSectionDto): ListSection {
        require(dto.type == "list_section") { "Unsupported dynamic section type: " + dto.type }
        val items = mapItems(dto.properties)
        val source = mapSource(dto.properties)
        require((items != null) xor (source != null)) {
            "Dynamic list_section requires exactly one of items or source"
        }
        val mappedGroups = dto.groups.map(groups::map)
        require(mappedGroups.isNotEmpty()) { "Dynamic list_section requires at least one row group" }

        return ListSection(
            id = dto.id,
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            weight = values.number(dto.properties, "weight"),
            items = items,
            source = source,
            filter = mapFilter(dto.properties),
            selection = mapSelection(dto.properties),
            itemSeparator = mapSeparator(dto.properties),
            groups = mappedGroups,
            emptySectionId = mapEmptySectionId(dto.properties),
        )
    }

    /** Parse the optional typed reference; ComponentMapper verifies its sibling identity. */
    private fun mapEmptySectionId(properties: JsonObject): String? {
        if ("emptySectionId" !in properties) return null
        val value = properties["emptySectionId"] as? JsonPrimitive
        require(value != null && value.isString && value.content.isNotBlank()) {
            "ListSection properties.emptySectionId must be a nonblank string"
        }
        return value.content
    }

    private fun mapItems(properties: JsonObject): List<Map<String, com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue>>? {
        val raw = properties["items"] ?: return null
        require(raw is JsonArray) { "Dynamic list_section items must be an array" }
        return raw.map { item ->
            require(item is JsonObject) { "Dynamic list_section items must contain objects" }
            values.toDynamicValueMap(item)
        }
    }

    private fun mapSource(properties: JsonObject): DynamicListSource? {
        val raw = properties["source"] ?: return null
        require(raw is JsonObject) { "Dynamic list_section source must be an object" }
        return when (values.string(raw, "type")) {
            "catalog" -> DynamicListSource.Catalog(
                key = values.string(raw, "key")?.takeIf(String::isNotBlank)
                    ?: error("Dynamic catalog source key is required"),
            )
            else -> throw IllegalArgumentException(
                "Unsupported dynamic list source type: " + values.string(raw, "type"),
            )
        }
    }

    private fun mapFilter(properties: JsonObject): DynamicListFilter? {
        val raw = properties["filter"] ?: return null
        require(raw is JsonObject) { "Dynamic list_section filter must be an object" }
        val fields = stringList(raw, "fields")
        require(fields.isNotEmpty()) { "Dynamic list filter fields are required" }
        return DynamicListFilter(
            binding = values.string(raw, "binding")?.takeIf(String::isNotBlank)
                ?: error("Dynamic list filter binding is required"),
            fields = fields,
            caseInsensitive = values.boolean(raw, "caseInsensitive"),
        )
    }

    private fun mapSelection(properties: JsonObject): DynamicListSelection? {
        val raw = properties["selection"] ?: return null
        require(raw is JsonObject) { "Dynamic list_section selection must be an object" }
        val mode = values.string(raw, "mode") ?: error("Dynamic list selection mode is required")
        require(mode == "single") { "Unsupported dynamic list selection mode: " + mode }
        val resultObject = raw["result"] as? JsonObject
            ?: error("Dynamic list selection result is required")
        val result = resultObject.entries.associate { (binding, item) ->
            val itemField = (item as? JsonPrimitive)?.contentOrNull
                ?: error("Dynamic list selection result values must be strings")
            binding to itemField
        }
        require(result.isNotEmpty()) { "Dynamic list selection result cannot be empty" }

        return DynamicListSelection(
            mode = mode,
            itemKey = values.string(raw, "itemKey")?.takeIf(String::isNotBlank)
                ?: error("Dynamic list selection itemKey is required"),
            selectedBinding = values.string(raw, "selectedBinding")?.takeIf(String::isNotBlank)
                ?: error("Dynamic list selection selectedBinding is required"),
            result = result,
            dismissOnSelect = values.boolean(raw, "dismissOnSelect")
                ?: error("Dynamic list selection dismissOnSelect is required"),
        )
    }

    private fun mapSeparator(properties: JsonObject): DynamicListSeparator? {
        val raw = properties["itemSeparator"] ?: return null
        require(raw is JsonObject) { "Dynamic list_section itemSeparator must be an object" }
        val type = values.string(raw, "type") ?: error("Dynamic list separator type is required")
        require(type == "divider") { "Unsupported dynamic list separator type: " + type }
        return DynamicListSeparator.Divider(
            orientation = values.string(raw, "orientation") ?: "horizontal",
            thickness = values.number(raw, "thickness")
                ?: error("Dynamic list divider thickness is required"),
            color = values.string(raw, "color")
                ?: error("Dynamic list divider color is required"),
            spacing = values.spacing(raw),
        )
    }

    private fun stringList(properties: JsonObject, key: String): List<String> {
        val raw = properties[key] as? JsonArray ?: return emptyList()
        return raw.map { item ->
            (item as? JsonPrimitive)?.contentOrNull
                ?: error("Dynamic '$key' values must be strings")
        }
    }
}
