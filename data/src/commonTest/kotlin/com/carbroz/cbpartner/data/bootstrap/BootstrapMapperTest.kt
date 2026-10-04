package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BootstrapMapperTest {
    private val mapper = BootstrapMapper()

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

        val failure = mapper.map(dto).exceptionOrNull()

        assertIs<BootstrapFailure.Api>(failure)
        assertEquals("INVALID_REQUEST", failure.code)
        assertEquals("Bootstrap request rejected", failure.message)
    }

    @Test
    fun startupDestinationMapsToCanonicalDynamicDestination() {
        val dto = Json.decodeFromString<BootstrapResponseDto>(
            """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "Partner bootstrap completed",
              "data": {
                "config": {
                  "version": "1",
                  "maintenance": {"enabled": false, "title": null, "message": null},
                  "update": {"required": false, "optional": false, "minimumVersion": "1.0.0", "latestVersion": "1.0.0", "storeUrl": null},
                  "features": {"registrationEnabled": true, "individualPartnerEnabled": true, "organizationPartnerEnabled": true}
                },
                "startup": {
                  "authenticated": false,
                  "destination": {
                    "screenId": "partner_login",
                    "templateId": "tpl_P101L",
                    "templateType": "form_template",
                    "endpoint": "/api/v1/partner/screen/auth_login"
                  }
                }
              },
              "traceId": "req-3"
            }
            """.trimIndent(),
        )

        val destination = mapper.map(dto).getOrThrow().startup.destination

        assertIs<DynamicDestination>(destination)
        assertEquals("partner_login", destination.screenId)
        assertEquals("tpl_P101L", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/api/v1/partner/screen/auth_login", destination.endpoint)
    }
}
