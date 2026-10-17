package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StateActionHandlerTest {
    private val handler = StateActionHandler()

    @Test
    fun stateReturnsTypedValueAndNoEffects() = runTest {
        val value = DynamicValue.NumberValue(7.0)
        val result = handler.handle(
            DynamicAction("state", mapOf("key" to DynamicValue.StringValue("count"), "value" to value)),
            emptyMap(), emptyMap(), emptyMap(),
        )
        assertEquals(mapOf("count" to value), result.stateChanges)
        assertEquals(emptyList(), result.effects)
    }

    @Test
    fun stateResolvesCurrentBinding() = runTest {
        val reference = DynamicValue.ObjectValue(mapOf(
            "\$binding" to DynamicValue.StringValue("mobileNumber"),
        ))
        val result = handler.handle(
            DynamicAction("state", mapOf("key" to DynamicValue.StringValue("phone"), "value" to reference)),
            mapOf("mobileNumber" to DynamicValue.StringValue("9876543210")),
            emptyMap(), emptyMap(),
        )
        assertEquals(DynamicValue.StringValue("9876543210"), result.stateChanges["phone"])
    }

    @Test
    fun absentValueIsNullAndAbsentKeyFails() = runTest {
        val result = handler.handle(
            DynamicAction("state", mapOf("key" to DynamicValue.StringValue("optional"))),
            emptyMap(), emptyMap(), emptyMap(),
        )
        assertEquals(DynamicValue.NullValue, result.stateChanges["optional"])
        assertFailsWith<IllegalStateException> {
            handler.handle(DynamicAction("state"), emptyMap(), emptyMap(), emptyMap())
        }
    }
}
