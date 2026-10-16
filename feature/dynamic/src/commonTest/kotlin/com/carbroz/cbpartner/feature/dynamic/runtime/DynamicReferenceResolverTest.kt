package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

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
                    mapOf("\$response" to DynamicValue.StringValue("data.reference")),
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
                    mapOf("\$unknown" to DynamicValue.StringValue("value")),
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
    @Test
    fun resolvesTypedBindingsWithoutStringCoercion() {
        val input = DynamicValue.ObjectValue(
            mapOf(
                "name" to ref("\$binding", "name"),
                "count" to ref("\$binding", "count"),
                "enabled" to ref("\$binding", "enabled"),
            ),
        )
        val resolved = DynamicReferenceResolver.resolve(
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
        val resolved = DynamicReferenceResolver.resolve(
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
    fun responseReferenceInActionPayloadIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicReferenceResolver.resolve(
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
        val resolved = DynamicReferenceResolver.resolve(
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
    @Test
    fun existingPathDistinguishesMissingAndExplicitNull() {
        val root = mapOf(
            "data" to DynamicValue.ObjectValue(
                mapOf("present" to DynamicValue.NullValue),
            ),
        )
        assertEquals(DynamicValue.NullValue, DynamicReferenceResolver.resolveExistingPath(root, "data.present"))
        assertEquals(null, DynamicReferenceResolver.resolveExistingPath(root, "data.missing"))
        assertEquals(null, DynamicReferenceResolver.resolveExistingPath(root, "data..present"))
        assertEquals(DynamicValue.NullValue, DynamicReferenceResolver.resolvePath(root, "data.missing"))
    }

}
