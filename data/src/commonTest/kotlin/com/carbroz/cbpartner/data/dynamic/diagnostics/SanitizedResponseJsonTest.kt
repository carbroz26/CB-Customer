package com.carbroz.cbpartner.data.dynamic.diagnostics

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SanitizedResponseJsonTest {
    @Test
    fun preservesNestedScreenStructureButRedactsFreeTextAndSecretValues() {
        val response = """
            {
              "status": 200,
              "message": "Secret free text",
              "data": {
                "screenId": "sample_screen",
                "template": {
                  "id": "tpl_X12",
                  "type": "form_template",
                  "components": [{
                    "type": "stack_component",
                    "elements": [{
                      "id": "submit_button",
                      "type": "button",
                      "properties": {
                        "text": "Submit private value",
                        "fontSize": 18,
                        "url": "https://private.example/token?secret=123"
                      },
                      "actions": {
                        "onClick": {
                          "type": "request",
                          "payload": {
                            "endpoint": "/api/v1/auth/private",
                            "body": {
                              "phoneNumber": "+919876543210",
                              "otp": "123456",
                              "token": "supersecret"
                            }
                          }
                        }
                      }
                    }]
                  }]
                }
              }
            }
        """.trimIndent()
        val output = sanitizedResponseJson(response).joinToString("\n")
        for (present in listOf("sample_screen", "tpl_X12", "form_template", "stack_component",
            "submit_button", "button", "onClick", "request", "fontSize", "18",
            "components", "elements", "[redacted]")) {
            assertTrue(output.contains(present), "Missing safe structure: $present")
        }
        for (secret in listOf("Secret free text", "Submit private value", "private.example",
            "+919876543210", "123456", "supersecret", "/api/v1/auth/private")) {
            assertFalse(output.contains(secret), "Leaked private value: $secret")
        }
    }

    @Test
    fun safeColorAlignmentAndUnknownFieldsStayUnambiguous() {
        val output = sanitizedResponseJson(
            """{"data":{"template":{"properties":{"color":"#13B8B5","horizontalAlignment":"center","fontSize":18,"untrustedA":"secret-one","untrustedB":"secret-two"}}}}"""
        ).joinToString("\\n")
        assertTrue(output.contains("#13B8B5"))
        assertTrue(output.contains("center"))
        assertTrue(output.contains("[unknown-fields-omitted]"))
        assertFalse(output.contains("untrustedA"))
        assertFalse(output.contains("untrustedB"))
        assertFalse(output.contains("secret-one"))
        assertFalse(output.contains("secret-two"))
    }

    @Test
    fun primitivePersonalDataIsRedactedEvenWhenNumeric() {
        val output = sanitizedResponseJson(
            """{"data":{"phoneNumber":919876543210,"otp":123456,"authenticated":false,"status":200}}"""
        ).joinToString("\n")
        assertFalse(output.contains("919876543210"))
        assertFalse(output.contains("123456"))
        assertTrue(output.contains("false"))
        assertTrue(output.contains("200"))
    }
}
