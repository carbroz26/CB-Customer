package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.app.validation.PartnerPhoneNumberValidator
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidatorRegistry
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Login-only app-composition and backend-confirmed region policy regressions. */
class ConsolidatedLoginIntegrationTest {
    @Test
    fun phoneNumberValidatorMatchesCurrentBackendIndianMobileAcceptance() {
        val validator = PartnerPhoneNumberValidator()
        val india = mapOf("region" to DynamicValue.StringValue("IN"))
        assertTrue(validator.isValid(DynamicValue.StringValue("9876543210"), india))
        assertTrue(validator.isValid(DynamicValue.StringValue("6123456789"), india))
        assertFalse(validator.isValid(DynamicValue.StringValue("5123456789"), india))
        assertFalse(validator.isValid(DynamicValue.StringValue("987654321"), india))
        assertFalse(validator.isValid(DynamicValue.StringValue("98765432101"), india))
        assertFalse(validator.isValid(DynamicValue.StringValue("98765 43210"), india))
    }

    @Test
    fun unsupportedOrMissingRegionFailsClosedWithoutGlobalLengthFallback() {
        val validator = PartnerPhoneNumberValidator()
        val phone = DynamicValue.StringValue("9876543210")
        assertFalse(validator.isValid(phone, mapOf("region" to DynamicValue.StringValue("US"))))
        assertFalse(validator.isValid(phone, emptyMap()))
        assertFalse(validator.isValid(phone, mapOf("region" to DynamicValue.NullValue)))
        assertFalse(validator.isValid(DynamicValue.NumberValue(9876543210.0),
            mapOf("region" to DynamicValue.StringValue("IN"))))
    }

    @Test
    fun namedValidatorRegistersOnceThroughExistingGenericRegistry() {
        val registry = DynamicValidatorRegistry(listOf<DynamicValidator>(PartnerPhoneNumberValidator()))
        assertTrue(
            registry.requireValidator("phone_number").isValid(
                DynamicValue.StringValue("9876543210"),
                mapOf("region" to DynamicValue.StringValue("IN")),
            ),
        )
    }
}
