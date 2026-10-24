package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.serialization.json.Json

/**
 * Opt-in, backend-free Dynamic preview fixture reused by Web and native Android.
 * Only prepares a typed response and development countries; rendering, actions,
 * state and validation remain owned by the normal application composition.
 */
data class LocalDynamicPreview(
    val response: DynamicResponse,
    val countries: List<Map<String, DynamicValue>>,
    val initialState: Map<String, DynamicValue> = emptyMap(),
)

/** Both previews pass their platform-loaded bytes through this exact DTO mapper. */
fun parseLocalDynamicPreview(
    raw: String,
    initialState: Map<String, DynamicValue> = emptyMap(),
): LocalDynamicPreview {
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val response = DynamicResponseMapper(json).map(
        json.decodeFromString<DynamicResponseDto>(raw),
    )
    requireNotNull(response.data) { "Local Dynamic preview must contain a screen" }
    return LocalDynamicPreview(response, previewCountries(), initialState)
}

/** UI-only development catalog, not a supported-region or backend country policy. */
private fun previewCountries(): List<Map<String, DynamicValue>> {
    fun country(id: String, name: String, dialCode: String, flagAsset: String) = mapOf(
        "id" to DynamicValue.StringValue(id),
        "name" to DynamicValue.StringValue(name),
        "dialCode" to DynamicValue.StringValue(dialCode),
        "flagAsset" to DynamicValue.StringValue(flagAsset),
    )
    return listOf(
        country("IN", "India", "+91", "flags/in"),
        country("US", "United States", "+1", "flags/us"),
        country("GB", "United Kingdom", "+44", "flags/gb"),
        country("AE", "United Arab Emirates", "+971", "flags/ae"),
        country("SA", "Saudi Arabia", "+966", "flags/sa"),
        country("QA", "Qatar", "+974", "flags/qa"),
        country("KW", "Kuwait", "+965", "flags/kw"),
        country("OM", "Oman", "+968", "flags/om"),
    )
}
