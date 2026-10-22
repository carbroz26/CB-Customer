package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListFilter
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSelection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicListItemsTest {
    private val india = mapOf(
        "id" to str("IN"), "name" to str("India"), "dialCode" to str("+91"),
        "flagAsset" to str("flags/in"),
    )
    private val usa = mapOf(
        "id" to str("US"), "name" to str("United States"), "dialCode" to str("+1"),
        "flagAsset" to str("flags/us"),
    )

    @Test
    fun inlineRowsKeepSourceOrderAndDeriveSelection() {
        val rows = evaluator().evaluate(
            section(items = listOf(usa, india)),
            mapOf("countryId" to str("IN"), "countrySearch" to str("")),
        )
        assertEquals(listOf("US", "IN"), rows.map { it.key })
        assertFalse(rows.first().selected)
        assertTrue(rows.last().selected)
    }

    @Test
    fun filterMatchesDeclaredFieldsWithCaseInsensitiveContainsAndBlankRestoresOrder() {
        val list = section(items = listOf(india, usa))
        val evaluator = evaluator()
        assertEquals(
            listOf("US"),
            evaluator.evaluate(list, mapOf("countrySearch" to str("united"))).map { it.key },
        )
        assertEquals(
            listOf("IN"),
            evaluator.evaluate(list, mapOf("countrySearch" to str("91"))).map { it.key },
        )
        assertEquals(
            listOf("IN", "US"),
            evaluator.evaluate(list, mapOf("countrySearch" to str(""))).map { it.key },
        )
        assertTrue(evaluator.evaluate(list, mapOf("countrySearch" to str("no match"))).isEmpty())
    }

    @Test
    fun catalogIsResolvedAndUnknownCatalogIsNotTreatedAsEmpty() {
        val catalog = DynamicListCatalog { key ->
            if (key == "countries") listOf(india, usa) else null
        }
        val evaluator = DynamicListItems(catalog)
        val section = section(items = null, source = DynamicListSource.Catalog("countries"))
        assertEquals(listOf("IN", "US"), evaluator.evaluate(section, emptyMap()).map { it.key })
        assertFailsWith<IllegalArgumentException> {
            evaluator.evaluate(section.copy(source = DynamicListSource.Catalog("missing")), emptyMap())
        }
    }

    @Test
    fun malformedKeysFilterFieldsAndMappedSelectionFieldsFailClosed() {
        val evaluator = evaluator()
        assertFailsWith<IllegalArgumentException> {
            evaluator.evaluate(section(listOf(india, india)), emptyMap())
        }
        assertFailsWith<IllegalStateException> {
            evaluator.evaluate(section(listOf(india - "id")), emptyMap())
        }
        assertFailsWith<IllegalArgumentException> {
            evaluator.evaluate(section(listOf(india - "name")), emptyMap())
        }
        assertFailsWith<IllegalArgumentException> {
            evaluator.evaluate(section(listOf(india - "flagAsset")), emptyMap())
        }
    }

    @Test
    fun searchBindingMustBeText() {
        assertFailsWith<IllegalArgumentException> {
            evaluator().evaluate(
                section(items = listOf(india)),
                mapOf("countrySearch" to DynamicValue.NumberValue(91.0)),
            )
        }
    }

    private fun evaluator(): DynamicListItems = DynamicListItems(DynamicListCatalog { null })

    private fun section(
        items: List<Map<String, DynamicValue>>? = null,
        source: DynamicListSource? = null,
    ) = ListSection(
        id = "countries",
        items = items,
        source = source,
        filter = DynamicListFilter(
            binding = "countrySearch",
            fields = listOf("name", "dialCode"),
            caseInsensitive = true,
        ),
        selection = DynamicListSelection(
            mode = "single",
            itemKey = "id",
            selectedBinding = "countryId",
            result = mapOf("countryId" to "id", "countryCode" to "dialCode", "countryFlagAsset" to "flagAsset"),
            dismissOnSelect = true,
        ),
    )

    private fun str(value: String) = DynamicValue.StringValue(value)
}
