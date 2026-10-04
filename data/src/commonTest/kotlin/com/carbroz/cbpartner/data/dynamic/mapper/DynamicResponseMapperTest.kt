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
    fun mapsDynamicResponseTreeActionPayloadAndNestedNextScreen() {
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
                              }
                            }
                          }
                        }]
                      }]
                    }]
                  }]
                },
                "nextScreen": {
                  "screenId": "partner_otp",
                  "templateId": "tpl_otp",
                  "templateType": "form_template",
                  "endpoint": "/otp",
                  "method": "GET",
                  "authentication": "NONE"
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
        val action = button.actions["onClick"]
        val nextScreen = assertNotNull(screen.nextScreen)

        assertEquals("partner_login", screen.screenId)
        assertEquals("form_template", screen.template.type)
        assertEquals("request", action?.type)
        assertEquals(
            DynamicValue.StringValue("/login"),
            action?.payload?.get("endpoint"),
        )
        assertEquals(
            DynamicValue.BooleanValue(true),
            action?.payload?.get("enabled"),
        )
        assertEquals("partner_otp", nextScreen.screenId)
        assertEquals("tpl_otp", nextScreen.templateId)
        assertEquals("form_template", nextScreen.templateType)
        assertEquals("/otp", nextScreen.endpoint)
        assertEquals("GET", nextScreen.method)
        assertEquals("NONE", nextScreen.authentication)
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
    }
}
