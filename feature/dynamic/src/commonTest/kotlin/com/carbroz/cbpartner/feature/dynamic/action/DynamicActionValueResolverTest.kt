package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicActionValueResolverTest {
    private companion object {
        const val DOLLAR = "$"
    }

    @Test
    fun resolvesBindingContextResponseAndLiteralReferences() {
        val previousResponse = DynamicValue.ObjectValue(
            mapOf("data" to DynamicValue.ObjectValue(
                mapOf("challengeId" to DynamicValue.StringValue("challenge-123")),
            )),
        )
        val input = DynamicValue.ObjectValue(
            mapOf(
                "binding" to DynamicValue.ObjectValue(mapOf("${DOLLAR}binding" to DynamicValue.StringValue("otp"))),
                "context" to DynamicValue.ObjectValue(mapOf("${DOLLAR}context" to DynamicValue.StringValue("authFlow.phoneNumber"))),
                "response" to DynamicValue.ObjectValue(mapOf("${DOLLAR}response" to DynamicValue.StringValue("data.challengeId"))),
                "literal" to DynamicValue.ObjectValue(mapOf("${DOLLAR}literal" to DynamicValue.StringValue("POST"))),
            ),
        )
        val resolved = DynamicActionValueResolver.resolve(
            value = input,
            bindings = mapOf("otp" to "123456"),
            context = mapOf("authFlow.phoneNumber" to "+919876543210"),
            previousResponse = previousResponse,
        ) as DynamicValue.ObjectValue

        assertEquals(DynamicValue.StringValue("123456"), resolved.value["binding"])
        assertEquals(DynamicValue.StringValue("+919876543210"), resolved.value["context"])
        assertEquals(DynamicValue.StringValue("challenge-123"), resolved.value["response"])
        assertEquals(DynamicValue.StringValue("POST"), resolved.value["literal"])
    }

    @Test
    fun missingResponseReferenceReturnsNullValue() {
        val resolved = DynamicActionValueResolver.resolve(
            value = DynamicValue.ObjectValue(mapOf("${DOLLAR}response" to DynamicValue.StringValue("data.challengeId"))),
            bindings = emptyMap(),
            context = emptyMap(),
            previousResponse = null,
        )
        assertIs<DynamicValue.NullValue>(resolved)
    }
}
