package com.carbroz.cbpartner.data.dynamic.model

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DynamicResponseMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsDynamicResponseStructureAndActionPayload() {
        val response = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "Partner login screen fetched successfully.",
              "data": {
                "screenId": "partner_login",
                "schemaVersion": "3.0.0",
                "targetApp": "PARTNER",
                "template": {
                  "id": "tpl_P101L",
                  "type": "form_template",
                  "properties": {
                    "orientation": "vertical",
                    "fillMaxSize": true
                  },
                  "components": [
                    {
                      "id": "login_content",
                      "type": "stack_component",
                      "properties": {},
                      "sections": [
                        {
                          "id": "action_section",
                          "type": "stack_section",
                          "properties": {},
                          "elements": [
                            {
                              "id": "continue_button",
                              "type": "button",
                              "properties": {
                                "text": "Continue"
                              },
                              "actions": {
                                "onClick": {
                                  "type": "request",
                                  "payload": {
                                    "method": "POST",
                                    "endpoint": "/api/v1/partner/auth/send_otp",
                                    "body": {
                                      "phoneNumber": {
                                        "${'$'}binding": "mobileNumber"
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              },
              "traceId": "req-1"
            }
            """,
        )

        val domain = DynamicResponseMapper().map(response)

        assertEquals("partner_login", domain.data?.screenId)
        assertEquals("vertical", domain.data?.template?.orientation)

        val button = domain.data!!.template.components
            .single()
            .sections
            .single()
            .elements
            .single()

        val action = assertNotNull(button.actions["onClick"])
        assertEquals("request", action.type)
        val method = assertNotNull(action.payload["method"])
        assertEquals("POST", (method as DynamicValue.StringValue).value)
        assertNotNull(action.payload["body"])
        assertEquals("req-1", domain.traceId)
    }

    @Test
    fun mapsActionResponseDestinationInsideData() {
        val response = json.decodeFromString<DynamicResponseDto>(
            """
            {
              "status": 200,
              "code": "OTP_SENT",
              "message": "OTP sent successfully.",
              "data": {
                "message": "OTP sent successfully",
                "challengeId": "challenge-1",
                "destination": {
                  "screenId": "partner_otp",
                  "templateId": "tpl_P101O",
                  "templateType": "form_template",
                  "endpoint": "/api/v1/partner/screen/auth_otp"
                }
              },
              "traceId": "req-2"
            }
            """,
        )

        val domain = DynamicResponseMapper().map(response)
        val destination = assertNotNull(domain.destination)

        assertEquals("OTP_SENT", domain.code)
        assertEquals("partner_otp", destination.screenId)
        assertEquals("tpl_P101O", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/api/v1/partner/screen/auth_otp", destination.endpoint)
        assertEquals("req-2", domain.traceId)
        assertEquals(null, domain.data)
        assertNotNull(domain.dataPayload)
    }
}
