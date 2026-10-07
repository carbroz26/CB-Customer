package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DynamicActionValueResolverTest {
    @Test
    fun resolvesTypedBindingsWithoutStringCoercion() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "name" to ref("\$binding", "name"),
                "count" to ref("\$binding", "count"),
                "enabled" to ref("\$binding", "enabled"),
            ),
        )
        val resolved = DynamicActionValueResolver.resolve(
            input,
            bindings = mapOf(
                "name" to DynamicValue.StringValue("Ada"),
                "count" to DynamicValue.NumberValue(2.0),
                "enabled" to DynamicValue.BooleanValue(true),
            ),
            context = emptyMap(),
            state = emptyMap(),
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf(
                    "name" to DynamicValue.StringValue("Ada"),
                    "count" to DynamicValue.NumberValue(2.0),
                    "enabled" to DynamicValue.BooleanValue(true),
                ),
            ),
            resolved,
        )
    }

    @Test
    fun resolvesNestedContextAndState() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "session" to ref("\$context", "session.id"),
                "reference" to ref("\$state", "flow.reference.id"),
            ),
        )
        val resolved = DynamicActionValueResolver.resolve(
            input,
            bindings = emptyMap(),
            context = mapOf(
                "session" to DynamicValue.ObjectValue(mapOf("id" to DynamicValue.StringValue("session-1"))),
            ),
            state = mapOf(
                "flow" to DynamicValue.ObjectValue(
                    mapOf("reference" to DynamicValue.ObjectValue(mapOf("id" to DynamicValue.StringValue("ref-1")))),
                ),
            ),
        )
        val objectValue = assertIs<DynamicValue.ObjectValue>(resolved)
        assertEquals(DynamicValue.StringValue("session-1"), objectValue.value["session"])
        assertEquals(DynamicValue.StringValue("ref-1"), objectValue.value["reference"])
    }

    @Test
    fun responseReferenceIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicActionValueResolver.resolve(
                ref("\$response", "data.referenceId"),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
    }

    @Test
    fun resolvesLiteralRecursively() {
        val input = ref("\$literal", DynamicValue.ObjectValue(mapOf("value" to ref("\$binding", "name"))))
        val resolved = DynamicActionValueResolver.resolve(
            input,
            bindings = mapOf("name" to DynamicValue.StringValue("resolved")),
            context = emptyMap(),
            state = emptyMap(),
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("value" to DynamicValue.StringValue("resolved"))),
            resolved,
        )
    }

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))

    private fun ref(type: String, value: DynamicValue) =
        DynamicValue.ObjectValue(mapOf(type to value))
}
