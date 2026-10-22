package com.carbroz.cbpartner.app.validation

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidator

/**
 * Partner-auth phone rule currently confirmed by backend PhoneNumber.create:
 * India-only 10 ASCII digits beginning 6–9. Other regions fail closed until a
 * vetted supported-region policy and compatible backend contract are supplied.
 */
class PartnerPhoneNumberValidator : DynamicValidator {
    override val key: String = "phone_number"

    override fun isValid(
        value: DynamicValue,
        arguments: Map<String, DynamicValue>,
    ): Boolean {
        val region = (arguments["region"] as? DynamicValue.StringValue)?.value
        val number = (value as? DynamicValue.StringValue)?.value
        return region == "IN" && number != null &&
            Regex("^[6-9][0-9]{9}$").matches(number)
    }
}
