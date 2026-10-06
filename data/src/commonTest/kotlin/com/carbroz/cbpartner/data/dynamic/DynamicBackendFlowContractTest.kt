package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.model.BootstrapResponseDto
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicBackendFlowContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dynamicMapper = DynamicResponseMapper()

    @Test
    fun bootstrapToDynamicScreenToNextDestinationFormsGenericFlow() {
        val bootstrap = BootstrapMapper().map(
            json.decodeFromString<BootstrapResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"Bootstrap completed","data":{"config":{"version":"1","maintenance":{"enabled":false,"title":null,"message":null},"update":{"required":false,"optional":false,"minimumVersion":"1.0.0","latestVersion":"1.0.0","storeUrl":null},"features":{"featureA":true,"featureB":false}},"startup":{"authenticated":false,"destination":{"screenId":"screen_generic","templateId":"tpl_generic","templateType":"form_template","endpoint":"/dynamic/screen"}}},"traceId":"trace-bootstrap"}""",
            ),
        ).getOrThrow()

        assertEquals("screen_generic", bootstrap.destination.screenId)
        assertEquals("tpl_generic", bootstrap.destination.templateId)
        assertEquals("form_template", bootstrap.destination.templateType)
        assertEquals("/dynamic/screen", bootstrap.destination.endpoint)

        val screen = assertNotNull(
            dynamicMapper.map(
                json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON),
            ).data,
        )
        assertEquals(bootstrap.destination.screenId, screen.screenId)
        assertEquals(bootstrap.destination.templateId, screen.template.id)

        val nextResponse = dynamicMapper.map(
            json.decodeFromString<DynamicResponseDto>(
                DynamicContractFixtures.GENERIC_RESPONSE_WITH_DESTINATION_JSON,
            ),
        )
        val next = assertNotNull(nextResponse.destination)
        assertEquals("screen_next", next.screenId)
        assertEquals("tpl_next", next.templateId)
        assertEquals("form_template", next.templateType)
        assertEquals("/next", next.endpoint)
    }
}
