package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldControlSurface
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationMessageStyle
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidationLengthMessage
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidationEquality
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DynamicFormValidatorTest {
    @Test
    fun returnsAllBindingErrorsAndSuppressesFallbackForPresentedBinding() {
        val screen = screen(
            inputs = listOf(
                input(
                    id = "phone",
                    binding = "phone",
                    validation = DynamicValidation(
                        required = true,
                        pattern = null,
                        message = "Phone required",
                    ),
                ),
                input(
                    id = "name",
                    binding = "name",
                    validation = DynamicValidation(
                        required = true,
                        pattern = null,
                        message = "Name required",
                    ),
                ),
            ),
            presentedBinding = "phone",
        )

        val result = DynamicFormValidator().validate(screen, emptyMap())

        assertEquals(
            mapOf(
                "phone" to "Phone required",
                "name" to "Name required",
            ),
            result.errors,
        )
        assertEquals("Name required", result.fallbackMessage)
    }

    @Test
    fun presentedErrorNeedsNoGenericFallback() {
        val result = DynamicFormValidator().validate(
            screen(
                inputs = listOf(
                    input(
                        id = "phone",
                        binding = "phone",
                        validation = DynamicValidation(
                            required = true,
                            pattern = null,
                            message = "Phone required",
                        ),
                    ),
                ),
                presentedBinding = "phone",
            ),
            emptyMap(),
        )

        assertEquals(mapOf("phone" to "Phone required"), result.errors)
        assertNull(result.fallbackMessage)
    }

    @Test
    fun validationMessagesRemainUntilThePhoneBecomesValid() {
        val phone = DynamicValidation(
            required = true, pattern = "^[6-9][0-9]{9}$",
            message = "Please enter a valid 10-digit mobile number.",
            requiredMessage = "Mobile number is required.",
            lengthMessages = listOf(
                DynamicValidationLengthMessage(6, 9, "Mobile number must be 10 digits."),
                DynamicValidationLengthMessage(minLength = 11, message = "Mobile number must be 10 digits."),
            ),
        )
        val validator = DynamicFormValidator()
        val screen = screen(listOf(input("phone", "phone", phone)))
        fun message(value: String) =
            validator.validate(screen, mapOf("phone" to DynamicValue.StringValue(value))).errors["phone"]
        assertEquals("Mobile number is required.", message(""))
        assertEquals("Please enter a valid 10-digit mobile number.", message("98765"))
        assertEquals("Mobile number must be 10 digits.", message("987654321"))
        assertEquals("Mobile number must be 10 digits.", message("987654321987"))
        assertEquals("Mobile number must be 10 digits.", message("98765432198"))
        assertNull(message("9876543219"))
        assertEquals("Please enter a valid 10-digit mobile number.", message("1234567890"))
    }


    @Test
    fun declarativeEqualityAndPatternPreserveIndiaOnlyRulesWithoutNamedValidators() {
        val policy = DynamicValidation(
            required = true,
            pattern = "^[6-9][0-9]{9}$",
            message = "Please enter a valid 10-digit mobile number.",
            requiredMessage = "Mobile number is required.",
            lengthMessages = listOf(
                DynamicValidationLengthMessage(6, 9, "Mobile number must be 10 digits."),
                DynamicValidationLengthMessage(minLength = 11, message = "Mobile number must be 10 digits."),
            ),
            clearOnEdit = true,
            equals = DynamicValidationEquality(
                DynamicValue.ObjectValue(mapOf("\$binding" to DynamicValue.StringValue("countryId"))),
                DynamicValue.ObjectValue(mapOf("\$literal" to DynamicValue.StringValue("IN"))),
            ),
        )
        val validator = DynamicFormValidator()
        val rendered = screen(listOf(input("mobile", "mobileNumber", policy)))
        fun error(phone: String, country: DynamicValue? = DynamicValue.StringValue("IN")): String? {
            val bindings = mutableMapOf<String, DynamicValue>(
                "mobileNumber" to DynamicValue.StringValue(phone),
            )
            if (country != null) bindings["countryId"] = country
            return validator.validate(rendered, bindings).errors["mobileNumber"]
        }
        assertNull(error("9876543210"))
        assertNull(error("6123456789"))
        assertEquals("Mobile number is required.", error(""))
        assertEquals("Please enter a valid 10-digit mobile number.", error("5123456789"))
        assertEquals("Please enter a valid 10-digit mobile number.", error("98765"))
        assertEquals("Mobile number must be 10 digits.", error("987654321"))
        assertEquals("Mobile number must be 10 digits.", error("98765432101"))
        // 11 characters including the space: existing length-based error copy applies.
        assertEquals("Mobile number must be 10 digits.", error("98765 43210"))
        // Exactly 10 characters but wrong format: generic pattern error copy applies.
        assertEquals("Please enter a valid 10-digit mobile number.", error("9876 43210"))
        assertEquals("Please enter a valid 10-digit mobile number.",
            error("9876543210", DynamicValue.StringValue("US")))
        assertEquals("Please enter a valid 10-digit mobile number.", error("9876543210", null))
        assertEquals("Please enter a valid 10-digit mobile number.",
            error("9876543210", DynamicValue.NullValue))
        assertEquals(true, validator.clearsErrorOnEdit(rendered, "mobileNumber"))
    }

    @Test
    fun equalityWorksOnArbitraryBindingsAndMissingValuesFailClosed() {
        val policy = DynamicValidation(
            required = false, pattern = null, message = "Values do not match",
            equals = DynamicValidationEquality(
                DynamicValue.ObjectValue(mapOf("\$binding" to DynamicValue.StringValue("first"))),
                DynamicValue.ObjectValue(mapOf("\$binding" to DynamicValue.StringValue("second"))),
            ),
        )
        val rendered = screen(listOf(input("confirm", "confirm", policy)))
        val values = mapOf(
            "confirm" to DynamicValue.StringValue("filled"),
            "first" to DynamicValue.StringValue("equal"),
            "second" to DynamicValue.StringValue("equal"),
        )
        assertEquals(emptyMap(), DynamicFormValidator().validate(rendered, values).errors)
        assertEquals(mapOf("confirm" to "Values do not match"),
            DynamicFormValidator().validate(rendered, values +
                ("second" to DynamicValue.StringValue("different"))).errors)
        assertEquals(mapOf("confirm" to "Values do not match"),
            DynamicFormValidator().validate(rendered, values - "first" - "second").errors)
    }

    @Test
    fun skippingEditedBindingDoesNotDisableSubmitOrOtherValidation() {
        val validation = DynamicValidation(
            required = true,
            pattern = "^[0-9]{10}$",
            message = "Invalid",
            clearOnEdit = true,
        )
        val field = screen(
            inputs = listOf(
                input("phone", "phone", validation),
                input("name", "name", DynamicValidation(required = true, pattern = null, message = "Name required")),
            ),
            presentedBinding = "phone",
        )
        val validator = DynamicFormValidator()
        assertEquals(true, validator.clearsErrorOnEdit(field, "phone"))
        assertEquals(false, validator.clearsErrorOnEdit(field, "name"))
        val invalid = mapOf(
            "phone" to DynamicValue.StringValue("123456789012"),
            "name" to DynamicValue.StringValue(""),
        )
        assertEquals(
            mapOf("phone" to "Invalid", "name" to "Name required"),
            validator.validate(field, invalid).errors,
        )
        val edited = validator.validate(field, invalid, suppressedBinding = "phone")
        assertEquals(mapOf("name" to "Name required"), edited.errors)
        assertEquals("Name required", edited.fallbackMessage)
    }

    private fun input(
        id: String,
        binding: String,
        validation: DynamicValidation,
    ) = InputElement(
        id = id,
        binding = DynamicBinding(binding),
        validation = validation,
    )

    private fun screen(
        inputs: List<InputElement>,
        presentedBinding: String? = null,
    ): DynamicScreen {
        val presented = inputs.singleOrNull { it.binding?.key == presentedBinding }
        val others = inputs.filterNot { it === presented }
        val sections = buildList<DynamicSection> {
            if (presented != null) {
                add(FormFieldSection(
                    id = "field",
                    label = TextElement(id = "label", text = "Test field"),
                    controlInput = presented,
                    inputBinding = requireNotNull(presentedBinding),
                    controlSurface = FormFieldControlSurface(height = 56.0, fillMaxWidth = true),
                    horizontalAlignment = DynamicArrangement.Start,
                    fillMaxWidth = true,
                    validationPresentation = DynamicValidationPresentation(
                        binding = requireNotNull(presentedBinding),
                        errorBorder = DynamicBorder(1.0, "#FF0000"),
                        messageStyle = DynamicValidationMessageStyle(fontSize = 14.0, lineHeight = 20.0),
                    ),
                ))
            }
            if (others.isNotEmpty()) {
                add(StackSection(id = "section", elements = others))
            }
        }
        return DynamicScreen(
            screenId = "screen",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(
                id = "template",
                components = listOf(
                    StackComponent(
                        id = "component",
                        sections = sections,
                    ),
                ),
            ),
            theme = null,
        )
    }
}
