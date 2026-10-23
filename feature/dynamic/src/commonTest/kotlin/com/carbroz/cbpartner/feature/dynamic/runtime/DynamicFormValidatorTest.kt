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
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidatorSpec
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun requiredThenPatternThenNamedValidatorRunInOrder() {
        var namedCalls = 0
        val named = object : DynamicValidator {
            override val key = "sample"

            override fun isValid(
                value: DynamicValue,
                arguments: Map<String, DynamicValue>,
            ): Boolean {
                namedCalls++
                return false
            }
        }
        val validator = DynamicFormValidator(DynamicValidatorRegistry(listOf(named)))
        val validation = DynamicValidation(
            required = true,
            pattern = "^[0-9]+$",
            message = "Invalid",
            validator = DynamicValidatorSpec("sample"),
        )
        val screen = screen(listOf(input("value", "value", validation)))

        validator.validate(screen, mapOf("value" to DynamicValue.StringValue("")))
        assertEquals(0, namedCalls)

        validator.validate(screen, mapOf("value" to DynamicValue.StringValue("abc")))
        assertEquals(0, namedCalls)

        validator.validate(screen, mapOf("value" to DynamicValue.StringValue("123")))
        assertEquals(1, namedCalls)
    }

    @Test
    fun namedValidatorReceivesResolvedArguments() {
        var region: DynamicValue? = null
        val named = object : DynamicValidator {
            override val key = "sample"

            override fun isValid(
                value: DynamicValue,
                arguments: Map<String, DynamicValue>,
            ): Boolean {
                region = arguments["region"]
                return true
            }
        }
        val validator = DynamicFormValidator(DynamicValidatorRegistry(listOf(named)))
        val validation = DynamicValidation(
            required = null,
            pattern = null,
            message = "Invalid",
            validator = DynamicValidatorSpec(
                key = "sample",
                arguments = mapOf(
                    "region" to DynamicValue.ObjectValue(
                        mapOf(
                            "\$binding" to DynamicValue.StringValue("countryId"),
                        ),
                    ),
                ),
            ),
        )

        val result = validator.validate(
            screen(listOf(input("phone", "phone", validation))),
            bindings = mapOf(
                "phone" to DynamicValue.StringValue("123"),
                "countryId" to DynamicValue.StringValue("IN"),
            ),
        )

        assertEquals(DynamicValue.StringValue("IN"), region)
        assertEquals(emptyMap(), result.errors)
    }

    @Test
    fun validationMessagesRemainUntilThePhoneBecomesValid() {
        val named = object : DynamicValidator {
            override val key = "phone"
            override fun isValid(value: DynamicValue, arguments: Map<String, DynamicValue>): Boolean =
                Regex("^[6-9][0-9]{9}$").matches((value as? DynamicValue.StringValue)?.value.orEmpty())
        }
        val phone = DynamicValidation(
            required = true, pattern = null,
            message = "Please enter a valid 10-digit mobile number.",
            validator = DynamicValidatorSpec("phone"),
            requiredMessage = "Mobile number is required.",
            lengthMessages = listOf(
                DynamicValidationLengthMessage(6, 9, "Mobile number must be 10 digits."),
                DynamicValidationLengthMessage(minLength = 11, message = "Mobile number must be 10 digits."),
            ),
        )
        val validator = DynamicFormValidator(DynamicValidatorRegistry(listOf(named)))
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
    fun registryRejectsDuplicateAndUnknownValidators() {
        val first = validator("same")
        val second = validator("same")
        assertFailsWith<IllegalStateException> {
            DynamicValidatorRegistry(listOf(first, second))
        }

        val form = DynamicFormValidator(DynamicValidatorRegistry(emptyList()))
        val screen = screen(
            listOf(
                input(
                    "value",
                    "value",
                    DynamicValidation(
                        required = null,
                        pattern = null,
                        message = "Invalid",
                        validator = DynamicValidatorSpec("missing"),
                    ),
                ),
            ),
        )
        assertFailsWith<IllegalArgumentException> {
            form.validate(
                screen,
                mapOf("value" to DynamicValue.StringValue("present")),
            )
        }
    }

    private fun validator(key: String) = object : DynamicValidator {
        override val key = key

        override fun isValid(
            value: DynamicValue,
            arguments: Map<String, DynamicValue>,
        ): Boolean = true
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
                        messageStyle = DynamicValidationMessageStyle(reserveHeight = 24.0),
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
