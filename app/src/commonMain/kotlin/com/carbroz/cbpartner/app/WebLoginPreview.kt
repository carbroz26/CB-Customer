package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Web-only, opt-in development input to the existing application composition.
 * The production bootstrap, Dynamic Store, renderers and catalog port stay unchanged.
 */
data class WebLoginPreview(
    val response: DynamicResponse,
    val countries: List<Map<String, DynamicValue>>,
)
