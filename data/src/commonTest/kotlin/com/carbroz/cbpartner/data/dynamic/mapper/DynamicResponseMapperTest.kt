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
    fun existingBodyOnlyScreenHasNoHeaderOrFooterSlots() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON)
        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        assertEquals(null, template.header)
        assertEquals(null, template.footer)
        assertEquals(2, template.components.size)
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
        assertEquals("phone_number", input.validation?.validator?.key)
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\$binding" to DynamicValue.StringValue("selectedId"))),
            input.validation?.validator?.arguments?.get("region"),
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

        val list = assertIs<ListSection>(surface.sections.single())
        assertEquals(1.0, list.weight)
        assertEquals("countries", assertIs<com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListSource.Catalog>(list.source).key)
        assertEquals("query", assertNotNull(list.filter).binding)
        assertEquals("single", assertNotNull(list.selection).mode)
        assertEquals("empty", assertIs<StackGroup>(assertNotNull(list.emptyGroup)).id)

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

}
