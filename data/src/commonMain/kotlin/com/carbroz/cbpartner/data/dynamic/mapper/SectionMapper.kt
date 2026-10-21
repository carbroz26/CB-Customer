package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.section.ListSectionDto
import com.carbroz.cbpartner.data.dynamic.model.section.StackSectionDto
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListFilter
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSelection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSeparator
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
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
        "list_section" -> map(json.decodeFromJsonElement(ListSectionDto.serializer(), node))
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
            emptyGroup = dto.emptyGroup?.let(groups::map),
        )
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
