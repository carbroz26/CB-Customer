package com.carbroz.cbpartner.domain.model.dynamic.section

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class ListSection(
    val id: String,
    val fillMaxWidth: Boolean = false,
    override val weight: Double? = null,
    val items: List<Map<String, DynamicValue>>? = null,
    val source: DynamicListSource? = null,
    val filter: DynamicListFilter? = null,
    val selection: DynamicListSelection? = null,
    val itemSeparator: DynamicListSeparator? = null,
    override val groups: List<DynamicGroup> = emptyList(),
    val emptySectionId: String? = null,
) : DynamicSection {
    override val elements: List<DynamicElement> = emptyList()
}

sealed interface DynamicListSource {
    data class Catalog(val key: String) : DynamicListSource
}

data class DynamicListFilter(
    val binding: String,
    val fields: List<String>,
    val caseInsensitive: Boolean? = null,
)

data class DynamicListSelection(
    val mode: String,
    val itemKey: String,
    val selectedBinding: String,
    val result: Map<String, String>,
    val dismissOnSelect: Boolean,
)

sealed interface DynamicListSeparator {
    data class Divider(
        val orientation: String = "horizontal",
        val thickness: Double,
        val color: String,
        val spacing: DynamicSpacing? = null,
    ) : DynamicListSeparator
}
