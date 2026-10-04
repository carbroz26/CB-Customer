package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
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
        assertEquals("form_template", screen.template.type)
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
