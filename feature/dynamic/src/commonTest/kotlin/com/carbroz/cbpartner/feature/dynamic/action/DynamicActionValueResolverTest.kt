package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicActionValueResolverTest {
    @Test
    fun resolvesMultipleBindings() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "first" to ref("$binding", "firstName"),
                "second" to ref("$binding", "email"),
            ),
        )

        val resolved = DynamicActionValueResolver.resolve(
            value = input,
            bindings = mapOf("firstName" to "Ada", "email" to "ada@example.com"),
            context = emptyMap(),
            previousResponse = null,
        )

        assertEquals(
            DynamicValue.ObjectValue(
                mapOf(
                    "first" to DynamicValue.StringValue("Ada"),
                    "second" to DynamicValue.StringValue("ada@example.com"),
                ),
            ),
            resolved,
        )
    }

    @Test
    fun resolvesContextInsideNestedObjectAndArray() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "session" to ref("$context", "session.id"),
                "items" to DynamicValue.ArrayValue(
                    listOf(
                        ref("$context", "device.id"),
                        DynamicValue.ObjectValue(
                            mapOf("region" to ref("$context", "device.region")),
                        ),
                    ),
                ),
            ),
        )

        val resolved = DynamicActionValueResolver.resolve(
            value = input,
            bindings = emptyMap(),
            context = mapOf(
                "session.id" to "session-1",
                "device.id" to "device-7",
                "device.region" to "west",
            ),
            previousResponse = null,
        )

        val objectValue = assertIs<DynamicValue.ObjectValue>(resolved)
        assertEquals(DynamicValue.StringValue("session-1"), objectValue.value["session"])
        val items = assertIs<DynamicValue.ArrayValue>(objectValue.value["items"])
        assertEquals(DynamicValue.StringValue("device-7"), items.value[0])
        val nested = assertIs<DynamicValue.ObjectValue>(items.value[1])
        assertEquals(DynamicValue.StringValue("west"), nested.value["region"])
    }

    @Test
    fun resolvesResponsePathAndMissingReferencesSafely() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "existing" to ref("$response", "data.reference.id"),
                "missing" to ref("$response", "data.unknown"),
                "noResponse" to ref("$response", "data.reference"),
            ),
        )
        val previous = DynamicValue.ObjectValue(
            mapOf(
                "data" to DynamicValue.ObjectValue(
                    mapOf(
                        "reference" to DynamicValue.ObjectValue(
                            mapOf("id" to DynamicValue.StringValue("ref-1")),
                        ),
                    ),
                ),
            ),
        )

        val resolved = DynamicActionValueResolver.resolve(
            value = input,
            bindings = emptyMap(),
            context = emptyMap(),
            previousResponse = previous,
        )
        val objectValue = assertIs<DynamicValue.ObjectValue>(resolved)

        assertEquals(DynamicValue.StringValue("ref-1"), objectValue.value["existing"])
        assertEquals(DynamicValue.NullValue, objectValue.value["missing"])
        assertEquals(DynamicValue.StringValue("ref-1"), objectValue.value["noResponse"])

        assertEquals(
            DynamicValue.NullValue,
            DynamicActionValueResolver.resolve(
                ref("$response", "data.reference"),
                emptyMap(),
                emptyMap(),
                null,
            ),
        )
    }

    @Test
    fun resolvesLiteralRecursively() {
        val input = ref(
            "$literal",
            DynamicValue.ObjectValue(
                mapOf(
                    "raw" to ref("$binding", "value"),
                ),
            ),
        )

        val resolved = DynamicActionValueResolver.resolve(
            value = input,
            bindings = mapOf("value" to "resolved"),
            context = emptyMap(),
            previousResponse = null,
        )

        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("raw" to DynamicValue.StringValue("resolved")),
            ),
            resolved,
        )
    }

    @Test
    fun leavesScalarsAndNullUntouched() {
        assertEquals(
            DynamicValue.StringValue("plain"),
            DynamicActionValueResolver.resolve(
                DynamicValue.StringValue("plain"),
                emptyMap(),
                emptyMap(),
                null,
            ),
        )
        assertEquals(
            DynamicValue.NullValue,
            DynamicActionValueResolver.resolve(
                DynamicValue.NullValue,
                emptyMap(),
                emptyMap(),
                null,
            ),
        )
    }

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))

    private fun ref(type: String, value: DynamicValue) =
        DynamicValue.ObjectValue(mapOf(type to value))
}
