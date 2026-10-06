package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.TextElement
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class DynamicResponseMapperTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun mapsDynamicResponseTreeActionPayloadAndDestinations() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "OK",
              "data": {
                "screenId": "partner_login",
                "schemaVersion": "3.0.0",
                "targetApp": "PARTNER",
                "template": {
                  "id": "tpl_login",
                  "type": "form_template",
                  "properties": {"padding": 16},
                  "components": [{
                    "id": "component",
                    "type": "stack_component",
                    "properties": {},
                    "sections": [{
                      "id": "section",
                      "type": "stack_section",
                      "properties": {},
                      "groups": [{
                        "id": "group",
                        "type": "stack_group",
                        "properties": {},
                        "elements": [{
                          "id": "login_button",
                          "type": "button",
                          "properties": {"text": "Login"},
                          "actions": {
                            "onClick": {
                              "type": "request",
                              "payload": {
                                "method": "POST",
                                "endpoint": "/login",
                                "enabled": true
                              },
                              "destination": {
                                "screenId": "partner_otp",
                                "templateId": "tpl_otp",
                                "templateType": "form_template",
                                "endpoint": "/otp"
                              }
                            }
                          }
                        }]
                      }]
                    }]
                  }]
                },
                "destination": {
                  "screenId": "partner_dashboard",
                  "templateId": "tpl_dashboard",
                  "templateType": "dashboard_template",
                  "endpoint": "/api/v1/partner/screen/dashboard"
                }
              },
              "traceId": "trace-1"
            }
            """.trimIndent(),
        )

        val response = DynamicResponseMapper().map(dto)
        val screen = assertNotNull(response.data)
        val button = screen.template.components
            .single().sections.single().groups.single().elements.single()
        val action = assertNotNull(button.actions["onClick"])
        val actionDestination = assertNotNull(action.destination)
        val responseDestination = assertNotNull(response.destination)

        assertEquals("partner_login", screen.screenId)
        assertEquals("vertical", screen.template.orientation)
        assertEquals("request", action.type)
        assertEquals(
            DynamicValue.StringValue("/login"),
            action.payload["endpoint"],
        )
        assertEquals(
            DynamicValue.BooleanValue(true),
            action.payload["enabled"],
        )
        assertEquals("partner_otp", actionDestination.screenId)
        assertEquals("tpl_otp", actionDestination.templateId)
        assertEquals("form_template", actionDestination.templateType)
        assertEquals("/otp", actionDestination.endpoint)
        assertEquals("partner_dashboard", responseDestination.screenId)
        assertEquals("tpl_dashboard", responseDestination.templateId)
        assertEquals("dashboard_template", responseDestination.templateType)
        assertEquals("/api/v1/partner/screen/dashboard", responseDestination.endpoint)
        assertNotNull(action)
    }

    @Test
    fun preservesArbitraryResponseDataForChainedActions() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "OTP_SENT",
              "message": "OTP sent",
              "data": {
                "challengeId": "challenge-1",
                "expiresIn": 60
              },
              "traceId": "trace-1"
            }
            """.trimIndent(),
        )

        val response = DynamicResponseMapper().map(dto)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)

        assertEquals(
            DynamicValue.StringValue("challenge-1"),
            payload.value["challengeId"],
        )
        assertEquals(
            DynamicValue.NumberValue(60.0),
            payload.value["expiresIn"],
        )
        assertEquals(null, response.data)
        assertEquals(null, response.destination)
    }
}


    @Test
    fun mapsAllKnownTypedUiModels() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "OK",
              "data": {
                "screenId": "typed",
                "schemaVersion": "3.0.0",
                "targetApp": "PARTNER",
                "template": {
                  "id": "tpl",
                  "type": "form_template",
                  "components": [{
                    "id": "component",
                    "type": "stack_component",
                    "sections": [{
                      "id": "section",
                      "type": "stack_section",
                      "groups": [{
                        "id": "group",
                        "type": "stack_group",
                        "elements": [
                          {"id":"text","type":"text","properties":{"text":"Hello"}},
                          {"id":"image","type":"image","properties":{"url":"https://example.com/a.png"}},
                          {"id":"input","type":"input","properties":{"keyboardType":"phone","maxLength":10},"binding":{"key":"phone"}},
                          {"id":"button","type":"button","properties":{"text":"Continue"}},
                          {"id":"icon","type":"icon","properties":{"name":"arrow_forward","size":20}},
                          {"id":"divider","type":"divider","properties":{"orientation":"horizontal","thickness":1}}
                        ]
                      }]
                    }]
                  }]
                }
              }
            }
            """.trimIndent(),
        )

        val screen = assertNotNull(DynamicResponseMapper().map(dto).data)
        assertIs<FormTemplate>(screen.template)
        assertIs<StackComponent>(screen.template.components.single())
        assertIs<StackSection>(screen.template.components.single().sections.single())
        assertIs<StackGroup>(screen.template.components.single().sections.single().groups.single())

        val elements = screen.template.components.single().sections.single().groups.single().elements
        assertIs<TextElement>(elements[0])
        assertIs<ImageElement>(elements[1])
        assertIs<InputElement>(elements[2])
        assertIs<ButtonElement>(elements[3])
        assertIs<IconElement>(elements[4])
        assertIs<DividerElement>(elements[5])
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "OK",
              "data": {
                "screenId": "unknown",
                "schemaVersion": "3.0.0",
                "targetApp": "PARTNER",
                "template": {
                  "id": "tpl",
                  "type": "form_template",
                  "components": [{
                    "id": "component",
                    "type": "stack_component",
                    "elements": [{"id":"unknown","type":"future_element"}]
                  }]
                }
              }
            }
            """.trimIndent(),
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper().map(dto)
        }
    }
