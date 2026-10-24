package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.DynamicContractFixtures
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ImageContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicLoadingIndicator
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class DynamicResponseMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsGenericScreenHierarchyAllElementsContentBindingsValidationAndActions() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_SCREEN_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        val screen = assertNotNull(response.data)
        val template = assertIs<FormTemplate>(screen.template)

        assertEquals("screen_generic", screen.screenId)
        assertEquals("3.0.0", screen.schemaVersion)
        assertEquals("PARTNER", screen.targetApp)
        assertEquals("tpl_generic", template.id)
        assertEquals("vertical", template.orientation)
        assertEquals(true, template.fillMaxSize)
        assertEquals(2, template.components.size)

        val header = assertIs<StackComponent>(template.components[0])
        assertEquals("header", header.id)
        assertEquals(3, header.elements.size)
        assertEquals(0, header.sections.size)

        val title = assertIs<TextElement>(header.elements[0])
        assertEquals("Welcome", title.text)
        assertEquals(1, title.spans.size)
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$context" to DynamicValue.StringValue("appVersion")),
            ),
            title.spans.single().text,
        )
        val leadingIcon = assertIs<IconContent>(title.leadingContent.single())
        assertEquals(DynamicSpacing(1.0, 2.0, 3.0, 4.0), leadingIcon.spacing)
        val trailingDivider = assertIs<DividerContent>(title.trailingContent.single())
        assertEquals(DynamicSpacing(5.0, 6.0, 7.0, 8.0), trailingDivider.spacing)
        assertEquals("navigate", title.actions["onClick"]?.type)

        val hero = assertIs<ImageElement>(header.elements[1])
        assertEquals("https://example.com/hero.png", hero.url)
        assertIs<IconContent>(hero.leadingContent.single())
        assertIs<IconContent>(hero.trailingContent.single())

        val icon = assertIs<IconElement>(header.elements[2])
        assertEquals("https://backend.example/assets/settings.png", icon.url)
        assertEquals(
            "https://backend.example/assets/fallback.png",
            icon.fallbackUrl,
        )
        assertEquals("toolbar", icon.semanticRole)

        val form = assertIs<StackComponent>(template.components[1])
        assertEquals("form", form.id)
        assertEquals(0, form.elements.size)
        assertEquals(2, form.sections.size)

        val identity = form.sections[0]
        assertIs<StackSection>(identity)
        assertEquals("identity", identity.id)
        assertEquals(2, identity.elements.size)

        val fieldA = assertIs<InputElement>(identity.elements[0])
        val fieldB = assertIs<InputElement>(identity.elements[1])
        assertEquals("fieldA", fieldA.binding?.key)
        assertEquals("fieldB", fieldB.binding?.key)
        assertEquals(true, fieldA.validation?.required)
        assertEquals("^[^@]+@[^@]+\\.[^@]+$", fieldB.validation?.pattern)

        val actions = assertIs<StackSection>(form.sections[1])
        assertEquals("actions", actions.id)
        assertEquals(0, actions.elements.size)
        assertEquals(1, actions.groups.size)

        val group = assertIs<StackGroup>(actions.groups.single())
        assertEquals("primary_group", group.id)
        val submit = assertIs<ButtonElement>(group.elements.single())
        assertEquals("Submit", submit.text)
        assertIs<IconContent>(submit.trailingContent.single())

        val request = assertNotNull(submit.actions["onClick"])
        assertEquals("request", request.type)
        val body = assertIs<DynamicValue.ObjectValue>(request.payload["body"])
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$binding" to DynamicValue.StringValue("fieldA")),
            ),
            body.value["fieldA"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$binding" to DynamicValue.StringValue("fieldB")),
            ),
            body.value["fieldB"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$context" to DynamicValue.StringValue("deviceId")),
            ),
            body.value["environmentId"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$state" to DynamicValue.StringValue("referenceId")),
            ),
            body.value["previous"],
        )
    }



    @Test
    fun mapsGenericAuthoredTextInputAndButtonLineHeightsWithoutDefaults() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{
                "screenId":"typography","schemaVersion":"3.0.0","targetApp":"PARTNER",
                "template":{"id":"form","type":"form_template","components":[{
                    "id":"example","type":"stack_component","elements":[
                        {"id":"heading","type":"text","properties":{
                            "text":"Welcome","fontSize":32,"lineHeight":40}},
                        {"id":"field","type":"input","binding":{"key":"field"},"properties":{
                            "placeholder":"Enter","fontSize":16,"lineHeight":24}},
                        {"id":"continue","type":"button","properties":{
                            "text":"Continue","fontSize":16,"lineHeight":24}}
                    ]
                }]}}}""",
        )
        val form = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        val elements = assertIs<StackComponent>(form.components.single()).elements
        assertEquals(40.0, assertIs<TextElement>(elements[0]).lineHeight)
        assertEquals(24.0, assertIs<InputElement>(elements[1]).lineHeight)
        assertEquals(24.0, assertIs<ButtonElement>(elements[2]).lineHeight)
    }

    @Test
    fun rejectsNonpositiveAuthoredLineHeightForTextInputAndButton() {
        for (type in listOf("text", "input", "button")) {
            val dto = json.decodeFromString<DynamicResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"OK","data":{
                    "screenId":"negative_typography","schemaVersion":"3.0.0","targetApp":"PARTNER",
                    "template":{"id":"form","type":"form_template","components":[{
                        "id":"example","type":"stack_component","elements":[
                            {"id":"field","type":"$type","properties":{"fontSize":16,"lineHeight":0}}
                        ]
                    }]}}}""",
            )
            assertFailsWith<IllegalArgumentException>("Invalid lineHeight for $type") {
                DynamicResponseMapper(json).map(dto)
            }
        }
    }

    @Test
    fun mapsOptionalHeaderFooterSlotsWithoutMovingBodyComponents() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"slots","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","properties":{"padding":{"all":20}},"header":{"properties":{"padding":{"all":8}},"components":[{"id":"title","type":"stack_component","elements":[{"id":"text","type":"text","properties":{"text":"Location"}}]}]},"components":[{"id":"body","type":"stack_component","elements":[{"id":"text","type":"text","properties":{"text":"Body"}}]}],"footer":{"properties":{"padding":{"all":12}},"components":[]}}}}""",
        )

        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        assertEquals("body", assertIs<StackComponent>(template.components.single()).id)
        assertEquals(20.0, template.padding?.top)
        val header = assertNotNull(template.header)
        assertEquals(8.0, header.padding?.top)
        assertEquals("title", assertIs<StackComponent>(header.components.single()).id)
        assertEquals("Location", assertIs<TextElement>(header.components.single().elements.single()).text)
        val footer = assertNotNull(template.footer)
        assertEquals(12.0, footer.padding?.top)
        assertEquals(0, footer.components.size)
    }

    @Test
    fun mapsBackendUrlBackgroundForEntireFormTemplateWithoutChangingTheme() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"sample","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"form","type":"form_template","properties":{"fillMaxSize":true,"backgroundImage":{"source":{"type":"url","value":"/assets/background.webp"},"contentScale":"crop","alignment":"topCenter","fallbackUrl":"/assets/backup.webp"}},"header":{"components":[]},"components":[{"id":"body","type":"stack_component"}],"footer":{"components":[]}},"theme":{"theme":"light","statusBar":"transparent","properties":{"gradient":{"angle":135,"colors":[{"color":"#123456","stop":0},{"color":"#654321","stop":1}]}}}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        val template = assertIs<FormTemplate>(screen.template)
        val background = assertNotNull(template.backgroundImage)
        val urlSource = assertIs<DynamicAssetSource.Url>(background.source)
        assertEquals(DynamicValue.StringValue("/assets/background.webp"), urlSource.value)
        assertEquals("crop", background.contentScale)
        assertEquals("topCenter", background.alignment)
        assertEquals("/assets/backup.webp", background.fallbackUrl)
        assertEquals(true, template.fillMaxSize)
        assertEquals(1, template.components.size)
        assertNotNull(template.header)
        assertNotNull(template.footer)
        assertNotNull(screen.theme?.properties?.get("gradient"))
        assertEquals(null, screen.theme?.properties?.get("backgroundImage"))
    }

    @Test
    fun mapsPackagedTemplateHeroWithoutChangingThemeOrUrlSupport() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"local_hero","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"hero","type":"form_template","properties":{"fillMaxSize":true,"backgroundImage":{"source":{"type":"resource","value":"images/partner_login_hero"},"contentScale":"crop","alignment":"topCenter"}},"components":[{"id":"form","type":"stack_component"}]},"theme":{"theme":"light","statusBar":"transparent","properties":{"gradient":{"angle":135,"colors":[{"color":"#FFFFFF","stop":0},{"color":"#D9F7F4","stop":1}]}}}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        val template = assertIs<FormTemplate>(screen.template)
        val background = assertNotNull(template.backgroundImage)
        val resource = assertIs<DynamicAssetSource.Resource>(background.source)
        assertEquals(DynamicValue.StringValue("images/partner_login_hero"), resource.value)
        assertEquals("crop", background.contentScale)
        assertEquals("topCenter", background.alignment)
        assertEquals(null, background.fallbackUrl)
        assertNotNull(screen.theme?.properties?.get("gradient"))
    }

    @Test
    fun rejectsInvalidTemplateBackgroundConfigurations() {
        fun response(raw: String): DynamicResponseDto = json.decodeFromString(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"sample","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"form","type":"form_template","properties":{"backgroundImage":$raw}}}}""",
        )
        val invalid = listOf(
            "null", "123", """{}""",
            """{"source":{"type":"resource","value":"images/sample"},"contentScale":"crop","alignment":"topCenter","fallbackUrl":"/assets/not-allowed.png"}""",
            """{"source":{"type":"url","value":"/asset"},"alignment":"topCenter"}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"crop"}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"unknown","alignment":"center"}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"crop","alignment":"wrong"}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"crop","alignment":5}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"crop","alignment":"center","fallbackUrl":null}""",
            """{"source":{"type":"url","value":"/asset"},"contentScale":"crop","alignment":"center","unexpected":1}""",
        )
        invalid.forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) {
                DynamicResponseMapper(json).map(response(value))
            }
        }
    }

    @Test
    fun existingBodyOnlyScreenHasNoHeaderOrFooterSlots() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON)
        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        assertEquals(null, template.header)
        assertEquals(null, template.footer)
        assertEquals(2, template.components.size)
        assertEquals(null, template.backgroundImage)
    }

    @Test
    fun unknownHeaderComponentTypeFailsWithoutDroppingContent() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"slots","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"body","type":"stack_component"}],"header":{"components":[{"id":"new","type":"unsupported_component"}]}}}}""",
        )
        assertFailsWith<IllegalArgumentException> { DynamicResponseMapper(json).map(dto) }
    }


    @Test
    fun imageElementRequiresNonBlankUrlButLegacyIconMayOmitUrl() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        for (properties in listOf("{}", """{"url":""}""", """{"url":"   "}""")) {
            val element = json.parseToJsonElement(
                """{"id":"hero","type":"image","properties":$properties}""",
            ).jsonObject
            assertFailsWith<IllegalArgumentException> { mapper.map(element) }
        }
        val icon = json.parseToJsonElement(
            """{"id":"legacy_icon","type":"icon","properties":{"name":"notifications"}}""",
        ).jsonObject
        assertEquals("", assertIs<IconElement>(mapper.map(icon)).url)
    }

    @Test
    fun malformedLeadingTrailingContentFailsRatherThanDroppingItems() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        for (content in listOf("""[{"type":"divider"},null]""", """[42]""", """"invalid"""", "null")) {
            val element = json.parseToJsonElement(
                """{"id":"text","type":"text","properties":{"text":"Hello","leading":$content}}""",
            ).jsonObject
            assertFailsWith<IllegalArgumentException> { mapper.map(element) }
        }
        val element = json.parseToJsonElement(
            """{"id":"text","type":"text","properties":{"text":"Hello","trailing":[{"type":"divider","properties":{"spacing":{"top":6}}}]}}""",
        ).jsonObject
        val text = assertIs<TextElement>(mapper.map(element))
        assertEquals(6.0, assertIs<DividerContent>(text.trailingContent.single()).spacing?.top)
    }

    @Test
    fun preservesNumericLookingJsonStringsAsStrings() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"phoneNumber":"+919309741971","otp":"111111","count":300}}""",
        )

        val response = DynamicResponseMapper(json).map(dto)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)

        assertEquals(
            DynamicValue.StringValue("+919309741971"),
            payload.value["phoneNumber"],
        )
        assertEquals(
            DynamicValue.StringValue("111111"),
            payload.value["otp"],
        )
        assertEquals(
            DynamicValue.NumberValue(300.0),
            payload.value["count"],
        )
    }

    @Test
    fun mapsArbitraryResponsePayloadWithoutTreatingItAsAScreen() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_RESPONSE_PAYLOAD_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        assertEquals(null, response.data)

        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("ref-123"), payload.value["referenceId"])
        assertEquals(DynamicValue.NumberValue(3.0), payload.value["count"])
        assertEquals(DynamicValue.BooleanValue(true), payload.value["enabled"])
        assertIs<DynamicValue.ArrayValue>(payload.value["items"])
        assertIs<DynamicValue.ObjectValue>(payload.value["nested"])
    }

    @Test
    fun mapsDestinationIndependentlyFromScreenPayload() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_RESPONSE_WITH_DESTINATION_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        val destination = assertNotNull(response.destination)
        assertEquals("screen_current", assertNotNull(response.data).screenId)
        assertEquals("screen_next", destination.screenId)
        assertEquals("tpl_next", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/next", destination.endpoint)
    }

    @Test
    fun preservesErrorResponsePayload() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_ERROR_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        assertEquals(422, response.status)
        assertEquals("VALIDATION_ERROR", response.code)
        assertEquals("Invalid input", response.message)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("fieldB"), payload.value["field"])
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"unknown","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"unknown","type":"future_element"}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun rejectsUnsupportedContainerTypes() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"future_component"}]}}}""",
        )

        assertFailsWith<IllegalArgumentException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun rejectsUnknownElementContentType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"button","type":"button","properties":{"leading":[{"type":"future_content"}]}}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun legacyMissingIconUrlsUseRendererFallbackWhileImageHasRequiredUrl() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"asset_fallback","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_assets","type":"form_template","components":[{"id":"assets","type":"stack_component","elements":[{"id":"icon","type":"icon","properties":{}},{"id":"image","type":"image","properties":{"url":"https://backend.example/assets/image.png"}},{"id":"label","type":"text","properties":{"text":"Label","textAlign":"center","fillMaxWidth":true,"leading":[{"type":"icon","properties":{}}],"trailing":[{"type":"divider","properties":{"orientation":"vertical","height":20,"thickness":1}}]}}]}]}}}""",
        )

        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        val component = assertIs<StackComponent>(assertIs<FormTemplate>(screen.template).components.single())
        assertEquals("", assertIs<IconElement>(component.elements[0]).url)
        assertEquals("https://backend.example/assets/image.png", assertIs<ImageElement>(component.elements[1]).url)
        val text = assertIs<TextElement>(component.elements[2])
        assertEquals("center", text.textAlign)
        assertEquals(true, text.fillMaxWidth)
        val leadingIcon = assertIs<IconContent>(text.leadingContent.single())
        assertEquals("", leadingIcon.url)
        assertEquals(null, leadingIcon.spacing)
        assertEquals(null, assertIs<DividerContent>(text.trailingContent.single()).spacing)
    }

    @Test
    fun categoryDispatchRejectsUnknownTemplateSectionAndGroup() {
        fun response(template: String): DynamicResponseDto = json.decodeFromString(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":$template}}""",
        )
        val templates = listOf(
            """{"id":"tpl","type":"future_template"}""",
            """{"id":"tpl","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"future_section"}]}]}""",
            """{"id":"tpl","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"stack_section","groups":[{"id":"g","type":"future_group"}]}]}]}""",
        )
        templates.forEach { template ->
            assertFailsWith<IllegalArgumentException> {
                DynamicResponseMapper(json).map(response(template))
            }
        }
    }

    @Test
    fun preservesGradientStopsForBackgroundMapping() {
        val value = DynamicValueMapper().background(
            json.parseToJsonElement(
                """{"background":{"type":"linearGradient","angle":135,"colors":[{"color":"#001122","stop":0},{"color":"#112233","stop":0.28},{"color":"#223344","stop":0.55},{"color":"#334455","stop":1}]}}"""
            ).jsonObject
        )
        val gradient = assertIs<DynamicBackground.LinearGradient>(value)
        assertEquals(135.0, gradient.angle)
        assertEquals(listOf(0.0, 0.28, 0.55, 1.0), gradient.colors.map { it.stop })
        assertEquals(listOf("#001122", "#112233", "#223344", "#334455"), gradient.colors.map { it.color })
    }

    @Test
    fun mapsAllFiveDeclaredEventHooksOnInputElement() {
        val payload = """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"events","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_events","type":"form_template","components":[{"id":"c","type":"stack_component","elements":[{"id":"input","type":"input","binding":{"key":"otp"},"properties":{},"actions":{"onClick":{"type":"navigate","payload":{"mode":"pop"}},"onLongClick":{"type":"dismiss"},"onValueChange":{"type":"state","payload":{"key":"v","value":1}},"onFocus":{"type":"dismiss"},"onSubmit":{"type":"dismiss"}}}] }]}}}"""
        val dto = json.decodeFromString<DynamicResponseDto>(payload)
        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        val input = assertIs<InputElement>(assertIs<StackComponent>(template.components.single()).elements.single())
        assertEquals(setOf("onClick", "onLongClick", "onValueChange", "onFocus", "onSubmit"), input.actions.keys)
        assertEquals("state", input.actions.getValue("onValueChange").type)
    }

    @Test
    fun categoryDispatchPreservesMultipleComponents() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"multi","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"first","type":"stack_component"},{"id":"second","type":"stack_component"}]}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        assertEquals("tpl", screen.template.id)
        assertEquals("form_template", screen.template.type)
        assertEquals(2, screen.template.components.size)
        assertEquals("first", assertIs<StackComponent>(screen.template.components[0]).id)
        assertEquals("second", assertIs<StackComponent>(screen.template.components[1]).id)
    }


    @Test
    fun mapsPhaseOneScreenContractWithoutRuntimeBehavior() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_PHASE1_SCREEN_JSON,
        )

        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        assertEquals(DynamicValue.StringValue(""), screen.initialBindings["query"])
        assertEquals(DynamicValue.StringValue("IN"), screen.initialBindings["selectedId"])

        val template = assertIs<FormTemplate>(screen.template)
        assertEquals(true, template.scrollable)

        val surface = assertIs<StackComponent>(template.components.single())
        assertEquals(DynamicBackground.Solid("#FFFFFF"), surface.background)
        assertEquals(
            DynamicShape.RoundedCorner(
                topStart = 24.0,
                topEnd = 24.0,
                bottomStart = 0.0,
                bottomEnd = 0.0,
            ),
            surface.shape,
        )

        val input = assertIs<InputElement>(surface.elements[0])
        assertEquals(true, input.singleLine)
        assertEquals("digits", input.inputFilter)
        assertEquals("#94A3B8", input.placeholderColor)
        assertEquals(44.0, input.height)
        assertEquals(DynamicBackground.Solid("#FFF1F2"), input.validationStyle?.background)
        assertEquals("^[6-9][0-9]{9}$", input.validation?.pattern)
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\$binding" to DynamicValue.StringValue("selectedId"))),
            input.validation?.equals?.value,
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\$literal" to DynamicValue.StringValue("IN"))),
            input.validation?.equals?.expected,
        )

        val button = assertIs<ButtonElement>(surface.elements[1])
        val loading = assertNotNull(button.loading)
        assertEquals(true, loading.whileActionPending)
        val indicator = assertIs<DynamicLoadingIndicator.Circular>(loading.indicator)
        assertEquals(20.0, indicator.size)

        val divider = assertIs<DividerElement>(surface.elements[2])
        assertEquals("vertical", divider.orientation)
        assertEquals(8.0, divider.spacing?.start)

        val label = assertIs<TextElement>(surface.elements[3])
        assertEquals(1.0, label.weight)
        assertEquals(44.0, label.minHeight)
        assertEquals(1, label.maxLines)
        assertEquals("ellipsis", label.overflow)
        assertIs<ImageContent>(label.leadingContent.single())

        assertEquals(2, surface.sections.size)
        val list = assertIs<ListSection>(surface.sections[0])
        assertEquals(1.0, list.weight)
        assertEquals("countries", assertIs<com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource.Catalog>(list.source).key)
        assertEquals("query", assertNotNull(list.filter).binding)
        assertEquals("single", assertNotNull(list.selection).mode)
        assertEquals("list_empty", list.emptySectionId)
        val emptySection = assertIs<StackSection>(surface.sections[1])
        assertEquals("list_empty", emptySection.id)
        val empty = assertIs<StackGroup>(emptySection.groups.single())
        assertEquals("empty", empty.id)
        assertEquals("empty_text", assertIs<TextElement>(empty.elements.single()).id)

        val row = assertIs<StackGroup>(list.groups.single())
        assertEquals(44.0, row.minHeight)
        assertEquals(DynamicBackground.Solid("#DCEBFC"), row.selectedStyle?.background)
        val image = assertIs<ImageElement>(row.elements[0])
        val resource = assertIs<DynamicAssetSource.Resource>(image.source)
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\$item" to DynamicValue.StringValue("asset"))),
            resource.value,
        )
        val indicatorIcon = assertIs<IconElement>(row.elements[1])
        assertEquals(true, indicatorIcon.visibleWhenSelected)
        assertEquals(true, indicatorIcon.reserveSpaceWhenHidden)

        val presentation = assertIs<BottomSheetPresentation>(screen.presentations.single())
        assertEquals("picker", presentation.id)
        assertEquals(true, presentation.dismissOnOutsideClick)
        assertEquals(true, presentation.dismissOnBack)
        assertEquals(true, presentation.showHandle)
        assertEquals(36.0, presentation.handle?.width)
        assertEquals(DynamicValue.StringValue(""), presentation.initialBindings["search"])
        assertEquals(1, presentation.components.size)
    }

    @Test
    fun rejectsDuplicateAndUnknownPresentations() {
        val duplicate = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"s","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template"},"presentations":[{"id":"p","type":"dialog","components":[{"id":"c1","type":"stack_component"}]},{"id":"p","type":"dialog","components":[{"id":"c2","type":"stack_component"}]}]}}""",
        )
        assertFailsWith<IllegalArgumentException> { DynamicResponseMapper(json).map(duplicate) }

        val unknown = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"s","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template"},"presentations":[{"id":"p","type":"popup","components":[{"id":"c","type":"stack_component"}]}]}}""",
        )
        assertFailsWith<IllegalArgumentException> { DynamicResponseMapper(json).map(unknown) }
    }

    @Test
    fun mapsStrictStandaloneDividersWithoutConflatingDividerContent() {
        val mapper = ElementMapper(json, DynamicValueMapper())

        fun map(properties: String, actions: String = ""): DividerElement =
            assertIs<DividerElement>(mapper.map(json.parseToJsonElement(
                """{"id":"phone_divider","type":"divider","properties":$properties$actions}"""
            ).jsonObject))

        val vertical = map(
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB","spacing":{"start":8,"end":12}}""",
            ""","actions":{"onClick":{"type":"state","payload":{"key":"selected","value":true}}}""",
        )
        assertEquals("vertical", vertical.orientation)
        assertEquals(null, vertical.width)
        assertEquals(28.0, vertical.height)
        assertEquals(1.0, vertical.thickness)
        assertEquals("#D1D5DB", vertical.color)
        assertEquals(DynamicSpacing(start = 8.0, end = 12.0), vertical.spacing)
        assertEquals("state", vertical.actions["onClick"]?.type)

        val horizontal = map(
            """{"orientation":"horizontal","width":32,"thickness":2,"color":"#FF223344"}""",
        )
        assertEquals(32.0, horizontal.width)
        assertEquals(null, horizontal.height)
        assertEquals(2.0, horizontal.thickness)
        assertEquals("#FF223344", horizontal.color)

        val content = ElementContentMapper(json, DynamicValueMapper()).map(
            json.parseToJsonElement(
                """{"type":"divider","properties":{"orientation":"vertical","height":20,"thickness":1}}"""
            ).jsonObject,
        )
        assertIs<DividerContent>(content)
    }

    @Test
    fun rejectsInvalidStandaloneDividerPayloadsRatherThanApplyingDefaults() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        val invalid = listOf(
            """{}""",
            """{"orientation":"vertical","height":28,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":28,"thickness":1}""",
            """{"orientation":"diagonal","height":28,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":0,"height":28,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","width":4,"height":28,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"horizontal","width":32,"height":null,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"horizontal","thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":0,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":-5,"thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":"28","thickness":1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":28,"thickness":0,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":28,"thickness":-1,"color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":28,"thickness":"1","color":"#D1D5DB"}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"red"}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#XYZXYZ"}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":123}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB","spacing":null}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB","spacing":{"start":-1}}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB","spacing":{"end":"12"}}""",
            """{"orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB","spacing":{"left":12}}""",
        )
        invalid.forEach { properties ->
            assertFailsWith<IllegalArgumentException>(properties) {
                mapper.map(json.parseToJsonElement(
                    """{"id":"invalid_divider","type":"divider","properties":$properties}"""
                ).jsonObject)
            }
        }
    }

    @Test
    fun directDomainDividerConstructorEnforcesGeometryAndSpacing() {
        assertFailsWith<IllegalArgumentException> {
            DividerElement(id = "invalid", orientation = "vertical",
                height = Double.POSITIVE_INFINITY, thickness = 1.0, color = "#D1D5DB")
        }
        assertFailsWith<IllegalArgumentException> {
            DividerElement(id = "invalid", orientation = "horizontal",
                width = 28.0, thickness = Double.NaN, color = "#D1D5DB")
        }
        assertFailsWith<IllegalArgumentException> {
            DividerElement(id = "invalid", orientation = "vertical",
                height = 28.0, thickness = 1.0, color = "#D1D5DB",
                spacing = DynamicSpacing(bottom = Double.NEGATIVE_INFINITY))
        }
        assertFailsWith<IllegalArgumentException> {
            DividerElement(id = "invalid", orientation = "horizontal",
                width = 28.0, height = 14.0, thickness = 1.0, color = "#D1D5DB")
        }
    }

    @Test
    fun mapsTypedImageContentAndRemoteImageElementWithoutLosingProperties() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        val image = assertIs<ImageElement>(mapper.map(json.parseToJsonElement(
            """{"id":"hero","type":"image","properties":{"source":{"type":"url","value":"/assets/hero.png"},"fallbackUrl":"/assets/backup.png","contentScale":"crop"}}"""
        ).jsonObject))
        assertIs<DynamicAssetSource.Url>(image.source)
        assertEquals("", image.url)
        assertEquals("/assets/backup.png", image.fallbackUrl)
        assertEquals("crop", image.contentScale)

        val label = assertIs<TextElement>(mapper.map(json.parseToJsonElement(
            """{"id":"title","type":"text","properties":{"text":"Brand","leading":[{"id":"logo","type":"image","properties":{"source":{"type":"url","value":"/assets/logo.png"},"fallbackUrl":"/assets/logo2.png","width":24,"height":18,"contentScale":"fit","spacing":{"end":8}}}]}}"""
        ).jsonObject))
        val accessory = assertIs<ImageContent>(label.leadingContent.single())
        assertIs<DynamicAssetSource.Url>(accessory.source)
        assertEquals("/assets/logo2.png", accessory.fallbackUrl)
        assertEquals(24.0, accessory.width)
        assertEquals(18.0, accessory.height)
        assertEquals(8.0, accessory.spacing?.end)
    }

    @Test
    fun rejectsInvalidTypedAssetsAndConflictingLegacyKeys() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        for (properties in listOf(
            """{"source":{"type":"resource","value":"images/logo"},"url":""}""",
            """{"source":{"type":"url","value":"/assets/image.png"},"url":"/assets/other.png"}""",
            """{"source":null,"url":"/assets/image.png"}""",
            """{"source":{"type":"resource","value":"images/logo"},"fallbackUrl":"/assets/no.png"}""",
            """{"source":{"type":"url","value":123}}""",
            """{"source":{"type":"url","value":""}}""",
            """{"source":{"type":"url","value":"/assets/image.png"},"contentScale":"unknown"}""",
            """{"fallbackUrl":"/assets/no.png"}""",
        )) {
            assertFailsWith<IllegalArgumentException> {
                mapper.map(json.parseToJsonElement(
                    """{"id":"hero","type":"image","properties":$properties}"""
                ).jsonObject)
            }
        }

        val contents = ElementContentMapper(json, DynamicValueMapper())
        for (properties in listOf(
            """{"url":"/assets/logo.png"}""",
            """{"source":{"type":"resource","value":"images/logo"},"url":""}""",
            """{"source":{"type":"resource","value":"images/logo"},"fallbackUrl":"/assets/no.png"}""",
            """{"source":{"type":"url","value":"/assets/logo.png"},"height":-1}""",
            """{"source":{"type":"url","value":"/assets/logo.png"},"contentScale":"not_supported"}""",
        )) {
            assertFailsWith<IllegalArgumentException> {
                contents.map(json.parseToJsonElement(
                    """{"type":"image","properties":$properties}"""
                ).jsonObject)
            }
        }
    }

    @Test
    fun buttonLoadingRequiresStrictApprovedFlagsAndRequestClick() {
        val mapper = ElementMapper(json, DynamicValueMapper())
        fun parse(properties: String, actions: String = """{"onClick":{"type":"request"}}""") =
            mapper.map(json.parseToJsonElement(
                """{"id":"submit","type":"button","properties":$properties,"actions":$actions}"""
            ).jsonObject)
        val withoutLoading = assertIs<ButtonElement>(parse("""{"text":"Send"}""", "{}"))
        assertEquals(null, withoutLoading.loading)
        val valid = assertIs<ButtonElement>(parse(
            """{"loading":{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}}"""
        ))
        val indicator = assertIs<DynamicLoadingIndicator.Circular>(assertNotNull(valid.loading).indicator)
        assertEquals(20.0, indicator.size)
        assertEquals("#FFFFFF", indicator.color)

        for (value in listOf(
            "null", "[]", "{}",
            """{"whileActionPending":false,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":false,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":false,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":"true","replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"linear","size":20,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":0,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":-1,"color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":"20","color":"#FFFFFF"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"red"}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":123}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF","extra":true}}""",
            """{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"},"unexpected":1}""",
        )) {
            assertFailsWith<IllegalArgumentException>(value) {
                parse("""{"loading":$value}""")
            }
        }
        val config = """{"loading":{"whileActionPending":true,"disableWhilePending":true,"replaceContent":true,"indicator":{"type":"circular","size":20,"color":"#FFFFFF"}}}"""
        for (invalidAction in listOf("{}", """{"onClick":{"type":"navigate"}}""",
                """{"onLongClick":{"type":"request"}}""",
                """{"onClick":{"type":"request"},"onLongClick":{"type":"dismiss"}}""")) {
            assertFailsWith<IllegalArgumentException> { parse(config, invalidAction) }
        }
    }

    @Test
    fun buttonIdentityRejectsDuplicatesInsideOneScopeButAllowsDifferentScopes() {
        fun map(parts: String) = DynamicResponseMapper(json).map(
            json.decodeFromString<DynamicResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"generic","schemaVersion":"3.0.0","targetApp":"PARTNER",$parts}}"""
            )
        )
        val element = """{"id":"submit","type":"button"}"""
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template","components":[{"id":"blank_component","type":"stack_component","elements":[{"id":"","type":"button"}]}]}""")
        }
        val component = """{"id":"c1","type":"stack_component","elements":[$element]}"""
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template","components":[$component,$component]}""")
        }
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template","header":{"components":[$component]},"components":[$component]}""")
        }
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template","components":[$component],"footer":{"components":[$component]}}""")
        }
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"stack_section","elements":[$element],"groups":[{"id":"g","type":"stack_group","elements":[$element]}]}]}]}""")
        }
        val presentation = """{"id":"p","type":"dialog","components":[$component]}"""
        assertNotNull(map(""""template":{"id":"t","type":"form_template","components":[$component]},"presentations":[$presentation]""").data)
        assertFailsWith<IllegalArgumentException> {
            map(""""template":{"id":"t","type":"form_template"},"presentations":[{"id":"p","type":"dialog","components":[$component,$component]}]""")
        }
    }

    @Test
    fun listSectionRequiresExactlyOneItemsSource() {
        fun response(properties: String): DynamicResponseDto = json.decodeFromString(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"s","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"list","type":"list_section","properties":$properties,"groups":[{"id":"row","type":"stack_group"}]}]}]}}}""",
        )

        assertFailsWith<IllegalArgumentException> {
            DynamicResponseMapper(json).map(response("""{}"""))
        }
        assertFailsWith<IllegalArgumentException> {
            DynamicResponseMapper(json).map(
                response("""{"items":[],"source":{"type":"catalog","key":"countries"}}"""),
            )
        }
    }


    /** Runs the same generic DTO -> Domain mapper used by Template and Presentation components. */
    private fun b5List(reference: String? = null, extra: String = ""): String {
        val value = reference?.let { ""","emptySectionId":$it""" }.orEmpty()
        return """{"id":"rows","type":"list_section","properties":{"items":[]$value},"groups":[{"id":"row","type":"stack_group"}]$extra}"""
    }

    private fun b5Stack(id: String = "fallback"): String =
        """{"id":"$id","type":"stack_section","groups":[{"id":"empty_group","type":"stack_group","elements":[{"id":"notice","type":"text","properties":{"text":"No results"}}]}]}"""

    private fun b5Component(sections: String, id: String = "main"): String =
        """{"id":"$id","type":"stack_component","sections":[$sections]}"""

    private fun b5Map(components: String, extraScreen: String = "") =
        DynamicResponseMapper(json).map(
            json.decodeFromString<DynamicResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"sample","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"template","type":"form_template","components":[$components]}$extraScreen}}""",
            ),
        )

    @Test
    fun b5ValidSiblingFallbackBeforeAfterAndOptionalReference() {
        val list = b5List("\"fallback\"")
        val stack = b5Stack()
        for (sections in listOf("$list,$stack", "$stack,$list")) {
            val screen = assertNotNull(b5Map(b5Component(sections)).data)
            val component = assertIs<StackComponent>(assertIs<FormTemplate>(screen.template).components.single())
            val mappedList = assertIs<ListSection>(component.sections.first { it is ListSection })
            assertEquals("fallback", mappedList.emptySectionId)
            val target = assertIs<StackSection>(component.sections.first { it is StackSection })
            assertEquals("fallback", target.id)
            assertEquals("empty_group", assertIs<StackGroup>(target.groups.single()).id)
            assertEquals("No results", assertIs<TextElement>(target.groups.single().elements.single()).text)
        }
        val noReference = assertNotNull(b5Map(b5Component(b5List())).data)
        val unmapped = assertIs<StackComponent>(assertIs<FormTemplate>(noReference.template).components.single())
        assertEquals(null, assertIs<ListSection>(unmapped.sections.single()).emptySectionId)
    }

    @Test
    fun b5InvalidReferenceTypesAndLegacyEmbeddedFallbackAreRejected() {
        for (invalid in listOf("null", "true", "0", "\"\"", "\"  \"", "{}", "[]")) {
            assertFailsWith<IllegalArgumentException>(invalid) {
                b5Map(b5Component("${b5List(invalid)},${b5Stack()}"))
            }
        }
        for (legacy in listOf(
            ""","emptyGroup":null""",
            ""","emptyGroup":{"id":"old","type":"stack_group"}""",
            ""","emptySectionId":"fallback"""",
        )) {
            assertFailsWith<IllegalArgumentException>(legacy) {
                b5Map(b5Component("${b5List("\"fallback\"", legacy)},${b5Stack()}"))
            }
        }
        assertFailsWith<IllegalArgumentException> {
            b5Map(b5Component("${b5List("\"fallback\"", ""","emptyGroup":{}""")},${b5Stack()}"))
        }
    }

    @Test
    fun b5ReferenceMustResolveExactlyOneStackSectionInSameComponent() {
        val list = b5List("\"fallback\"")
        val target = b5Stack()
        assertFailsWith<IllegalArgumentException> { b5Map(b5Component(list)) }
        assertFailsWith<IllegalArgumentException> { b5Map(b5Component("$list,$target,$target")) }
        assertFailsWith<IllegalArgumentException> {
            b5Map("${b5Component(list)},${b5Component(target, "other")}")
        }
        assertFailsWith<IllegalArgumentException> {
            b5Map(b5Component(b5List("\"rows\"")))
        }
        val wrongType = b5List().replace("\"id\":\"rows\"", "\"id\":\"fallback\"")
        assertFailsWith<IllegalArgumentException> {
            b5Map(b5Component("$list,$wrongType"))
        }
        val presentation = ""","presentations":[{"id":"popup","type":"dialog","components":[${b5Component("$list,$target", "modal")}]}]"""
        assertNotNull(b5Map(b5Component("$list,$target"), presentation).data)
        assertFailsWith<IllegalArgumentException> {
            b5Map(b5Component(list), presentation)
        }
        assertNotNull(b5Map(
            "${b5Component("$list,$target", "first")},${b5Component("$list,$target", "second")}",
        ).data)
        val invalidModal = ""","presentations":[{"id":"popup","type":"dialog","components":[${b5Component(list, "modal")}]}]"""
        assertFailsWith<IllegalArgumentException> {
            b5Map(b5Component("$list,$target"), invalidModal)
        }
    }



    @Test
    fun b5ReferenceDoesNotCrossHeaderOrFooterComponentBoundaries() {
        val list = b5List("\"fallback\"")
        for (slotName in listOf("header", "footer")) {
            val slot = "\"$slotName\":{\"components\":[${b5Component(b5Stack(), "slot_component")}]}"
            val payload = """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"s","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"t","type":"form_template","components":[${b5Component(list)}],$slot}}}"""
            assertFailsWith<IllegalArgumentException>(slotName) {
                DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(payload))
            }
        }
    }


}
