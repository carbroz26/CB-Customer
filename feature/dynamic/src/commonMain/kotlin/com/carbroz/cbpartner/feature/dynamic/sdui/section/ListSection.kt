package com.carbroz.cbpartner.feature.dynamic.sdui.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSeparator
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection as ListSectionModel
import com.carbroz.cbpartner.feature.dynamic.registry.SectionRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

/**
 * Lazy Section renderer. Source/filter/selection identity are evaluated by the shared
 * pure list evaluator. A backend-authored weight grants a finite parent viewport.
 * No screen-specific colors, data, dimensions or state live here.
 */
class ListSection : SectionRenderer<ListSectionModel> {
    @Composable
    override fun render(section: ListSectionModel, context: DynamicRenderContext) {
        // The enclosing StackComponent renders weighted sections inside a bounded Box.
        require(section.weight != null) {
            "Lazy ListSection requires backend-authored weight for a bounded viewport"
        }
        val rows = context.listRows(section)
        if (rows.isEmpty()) {
            section.emptySectionId?.let { id ->
                val fallback = requireNotNull(context.listFallbackSections[id]) {
                    "ListSection fallback must reference a sibling StackSection"
                }
                context.renderSection(fallback)
            }
            return
        }
        val separator = section.itemSeparator
        val base = if (section.fillMaxWidth) Modifier.fillMaxWidth() else Modifier
        LazyColumn(modifier = base.fillMaxHeight()) {
            itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
                val rowContext = context.withListRow(row.item, row.selected)
                val clickable = if (section.selection == null) Modifier else {
                    Modifier.clickable {
                        context.onListItemSelected(section.id, row.key)
                    }
                }
                Column(modifier = clickable) {
                    section.groups.forEach { group ->
                        rowContext.renderGroup(group)
                    }
                }
                if (index != rows.lastIndex && separator != null) {
                    val divider = separator as? DynamicListSeparator.Divider
                        ?: error("Unsupported Dynamic list separator")
                    require(divider.orientation == "horizontal") {
                        "Unsupported Dynamic list separator orientation"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .applyDynamicSpacing(divider.spacing)
                            .height(divider.thickness.dp)
                            .background(
                                requireNotNull(divider.color.toColor()) {
                                    "Invalid Dynamic list separator color"
                                },
                            ),
                    )
                }
            }
        }
    }
}
