package com.carbroz.cbpartner.data.dynamic.diagnostics

import com.carbroz.cbpartner.data.dynamic.DynamicContractFixtures
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class SduiTraceProjectionTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun realMappedFixtureRetainsSixCategoryHierarchyWithoutContentValues() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON)
        val screen = requireNotNull(DynamicResponseMapper(json).map(dto).data)
        val lines = projectSduiStructure(screen)
        assertTrue(lines.any { it.contains("category=template type=form_template") })
        assertTrue(lines.any { it.contains("category=component type=stack_component") })
        assertTrue(lines.any { it.contains("category=section type=stack_section") })
        assertTrue(lines.any { it.contains("category=group type=stack_group") })
        assertTrue(lines.any { it.contains("category=element type=button") })
        assertTrue(lines.any { it.contains("category=content type=icon") })
        val joined = lines.joinToString("\n")
        for (secret in listOf("Welcome", "Submit", "fieldA", "fieldB", "example.com", "referenceId", "deviceId")) {
            assertFalse(joined.contains(secret), "Leaked field or payload value: $secret")
        }
    }

    @Test
    fun projectionNodeLimitIsBoundedAndExplicit() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON)
        val screen = requireNotNull(DynamicResponseMapper(json).map(dto).data)
        val lines = projectSduiStructure(screen, limit = 2)
        assertTrue(lines.size <= 3)
        assertTrue(lines.last().contains("event=sdui.truncated"))
    }
}
