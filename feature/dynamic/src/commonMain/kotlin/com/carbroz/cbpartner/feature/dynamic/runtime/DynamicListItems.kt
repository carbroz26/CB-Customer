package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/** An evaluated source row; key is stable within the immutable catalog/inline snapshot. */
internal data class DynamicListRow(
    val key: String,
    val item: Map<String, DynamicValue>,
    val selected: Boolean,
)

/**
 * Pure ListSection data evaluation shared by rendering and Store selection validation.
 * Never sorts, changes bindings, dismisses presentations or owns mutable UI state.
 */
internal class DynamicListItems(private val catalog: DynamicListCatalog) {
    fun evaluate(
        section: ListSection,
        bindings: Map<String, DynamicValue>,
    ): List<DynamicListRow> {
        require((section.items != null) xor (section.source != null)) {
            "ListSection requires exactly one inline or catalog source"
        }
        val records = when (val source = section.source) {
            is DynamicListSource.Catalog ->
                requireNotNull(catalog.items(source.key)) { "Dynamic list catalog is unavailable" }
            null -> requireNotNull(section.items)
        }
        val selection = section.selection
        val filter = section.filter
        val query = filter?.let {
            val value = bindings[it.binding] ?: DynamicValue.StringValue("")
            require(value is DynamicValue.StringValue) { "Dynamic list search binding must be text" }
            if (it.caseInsensitive == true) value.value.lowercase() else value.value
        }
        val selectedValue = selection?.let { bindings[it.selectedBinding] }
        val ids = mutableSetOf<String>()
        return records.mapIndexed { index, record ->
            val fields = record.toMap()
            val key = if (selection == null) {
                index.toString()
            } else {
                (fields[selection.itemKey] as? DynamicValue.StringValue)?.value
                    ?.takeIf(String::isNotBlank)
                    ?: error("Dynamic list item key must be a nonblank string")
            }
            require(ids.add(key)) { "Duplicate Dynamic list item key" }

            selection?.result?.values?.forEach { field ->
                require(fields[field] != null) { "Dynamic list selection field is missing" }
            }
            filter?.fields?.forEach { field ->
                require(fields[field] is DynamicValue.StringValue) {
                    "Dynamic list filter field must exist as text"
                }
            }
            DynamicListRow(
                key = key,
                item = fields,
                selected = selection != null && selectedValue != null &&
                    selectedValue == fields[selection.itemKey],
            )
        }.filter { row ->
            filter == null || query.isNullOrBlank() || filter.fields.any { field ->
                val candidate = (row.item.getValue(field) as DynamicValue.StringValue).value
                val compared = if (filter.caseInsensitive == true) candidate.lowercase() else candidate
                compared.contains(query)
            }
        }
    }
}
