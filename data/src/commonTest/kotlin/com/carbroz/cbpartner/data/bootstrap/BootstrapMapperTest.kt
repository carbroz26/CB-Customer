package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BootstrapMapperTest {
    @Test
    fun apiFailureCodeAndMessageAreMapped() {
        val dto = Json.decodeFromString<BootstrapResponseDto>(
            """
            {
              "status": 200,
              "code": "INVALID_REQUEST",
              "message": "Bootstrap request rejected",
              "data": null,
              "traceId": "req-error"
            }
            """.trimIndent(),
        )

        val failure = dto.toDomain().exceptionOrNull()

        assertIs<BootstrapFailure.Api>(failure)
        assertEquals("INVALID_REQUEST", failure.code)
        assertEquals("Bootstrap request rejected", failure.message)
    }

    @Test
    fun startupNextScreenMapsToCanonicalDynamicDestination() {
        val dto = Json.decodeFromString<BootstrapResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "Partner bootstrap completed",
              "data": {
                "config": {
                  "version": "1",
                  "maintenance": {
                    "enabled": false,
                    "title": null,
                    "message": null
                  },
                  "update": {
                    "required": false,
                    "optional": false,
                    "minimumVersion": "1.0.0",
                    "latestVersion": "1.0.0",
                    "storeUrl": null
                  },
                  "features": {
                    "registrationEnabled": true,
                    "individualPartnerEnabled": true,
                    "organizationPartnerEnabled": true
                  }
                },
                "startup": {
                  "authenticated": false,
                  "nextScreen": {
                    "screenId": "partner_login",
                    "templateId": "tpl_7K2M9Q",
                    "templateType": "stack_template",
                    "endpoint": "/api/v1/partner/screen/auth_login",
                    "method": "GET",
                    "authentication": "NONE"
                  }
                }
              },
              "traceId": "req-3"
            }
            """.trimIndent(),
        )

        val output = dto.toDomain().getOrThrow()
        val destination = output.startup.nextScreen

        assertIs<DynamicDestination>(destination)
        assertEquals("partner_login", destination.screenId)
        assertEquals("tpl_7K2M9Q", destination.templateId)
        assertEquals("stack_template", destination.templateType)
        assertEquals("/api/v1/partner/screen/auth_login", destination.endpoint)
        assertEquals("GET", destination.method)
        assertEquals("NONE", destination.authentication)
    }
}
