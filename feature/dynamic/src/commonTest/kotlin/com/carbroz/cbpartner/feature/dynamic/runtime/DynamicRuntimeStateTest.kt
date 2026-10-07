package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicRuntimeStateTest {
    @Test
    fun storesAndReadsTypedNestedValues() {
        val state = DynamicRuntimeState()

        state.setAll(
            mapOf(
                "flow.reference.id" to DynamicValue.StringValue("ref-1"),
                "flow.count" to DynamicValue.NumberValue(3.0),
                "enabled" to DynamicValue.BooleanValue(true),
                "items" to DynamicValue.ArrayValue(
                    listOf(DynamicValue.StringValue("a"), DynamicValue.NumberValue(2.0)),
                ),
            ),
        )

        assertEquals(
            DynamicValue.StringValue("ref-1"),
            state.get("flow.reference.id"),
        )
        assertEquals(DynamicValue.NumberValue(3.0), state.get("flow.count"))
        assertEquals(DynamicValue.BooleanValue(true), state.get("enabled"))
        assertEquals(
            DynamicValue.ArrayValue(
                listOf(DynamicValue.StringValue("a"), DynamicValue.NumberValue(2.0)),
            ),
            state.get("items"),
        )
    }

    @Test
    fun laterNestedWritePreservesSiblingValues() {
        val state = DynamicRuntimeState()

        state.setAll(
            mapOf(
                "flow.reference.id" to DynamicValue.StringValue("ref-1"),
                "flow.reference.type" to DynamicValue.StringValue("generic"),
            ),
        )
        state.set("flow.reference.id", DynamicValue.StringValue("ref-2"))

        assertEquals(DynamicValue.StringValue("ref-2"), state.get("flow.reference.id"))
        assertEquals(DynamicValue.StringValue("generic"), state.get("flow.reference.type"))
    }
}
