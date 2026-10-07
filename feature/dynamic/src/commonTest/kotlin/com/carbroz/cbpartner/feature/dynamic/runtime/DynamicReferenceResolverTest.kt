package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DynamicReferenceResolverTest {
    @Test
    fun resolvesNestedTypedStateWithoutStringification() {
        val state = mapOf(
            "flow" to DynamicValue.ObjectValue(
                mapOf(
                    "count" to DynamicValue.NumberValue(3.0),
                    "enabled" to DynamicValue.BooleanValue(true),
                ),
            ),
        )

        assertEquals(
            DynamicValue.NumberValue(3.0),
            DynamicReferenceResolver.resolve(
                DynamicValue.ObjectValue(mapOf("\$state" to DynamicValue.StringValue("flow.count"))),
                emptyMap(),
                emptyMap(),
                state,
            ),
        )
        assertEquals(
            DynamicValue.BooleanValue(true),
            DynamicReferenceResolver.resolve(
                DynamicValue.ObjectValue(mapOf("\$state" to DynamicValue.StringValue("flow.enabled"))),
                emptyMap(),
                emptyMap(),
                state,
            ),
        )
    }

    @Test
    fun rejectsResponseNamespace() {
        assertFailsWith<IllegalStateException> {
            DynamicReferenceResolver.resolve(
                DynamicValue.ObjectValue(
                    mapOf("$response" to DynamicValue.StringValue("data.reference")),
                ),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
    }

    @Test
    fun rejectsUnknownReferenceNamespace() {
        assertFailsWith<IllegalStateException> {
            DynamicReferenceResolver.resolve(
                DynamicValue.ObjectValue(
                    mapOf("$unknown" to DynamicValue.StringValue("value")),
                ),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
    }

    @Test
    fun preservesObjectReferencesAsObjectValues() {
        val value = DynamicReferenceResolver.resolve(
            DynamicValue.ObjectValue(
                mapOf(
                    "nested" to DynamicValue.ObjectValue(
                        mapOf("\$context" to DynamicValue.StringValue("environment")),
                    ),
                ),
            ),
            emptyMap(),
            mapOf(
                "environment" to DynamicValue.ObjectValue(
                    mapOf("id" to DynamicValue.StringValue("environment-1")),
                ),
            ),
            emptyMap(),
        )

        assertEquals(
            DynamicValue.ObjectValue(
                mapOf(
                    "nested" to DynamicValue.ObjectValue(
                        mapOf("id" to DynamicValue.StringValue("environment-1")),
                    ),
                ),
            ),
            value,
        )
    }
}
