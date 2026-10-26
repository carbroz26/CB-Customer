package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.model.BootstrapResponseDto
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.serialization.json.Json

/** Opt-in development-fixture duration only; production uses backend response values. */
const val LOCAL_PREVIEW_RESEND_SECONDS = 10.0

data class LocalDynamicPreview(
    val response: DynamicResponse,
    val countries: List<Map<String, DynamicValue>>,
    val initialState: Map<String, DynamicValue> = emptyMap(),
    val resendFailureMode: Boolean = false,
    val networkFailure: AppFailure.Category? = null,
    /** Only an explicit dev preview may provide the next screen without Backend. */
    val nextScreen: DynamicResponse? = null,
) {
    /** Keep Domain response mapping inside the app boundary; platform hosts supply only JSON. */
    fun withNextScreenFixture(raw: String): LocalDynamicPreview =
        copy(nextScreen = parseLocalDynamicPreview(raw).response)
}

/** Both previews pass their platform-loaded bytes through this exact DTO mapper. */
fun parseLocalDynamicPreview(
    raw: String,
    initialState: Map<String, String> = emptyMap(),
    numericInitialState: Map<String, Double> = emptyMap(),
    resendFailureMode: Boolean = false,
    networkFailure: String? = null,
): LocalDynamicPreview {
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val response = DynamicResponseMapper(json).map(
        json.decodeFromString<DynamicResponseDto>(raw),
    )
    requireNotNull(response.data) { "Local Dynamic preview must contain a screen" }
    return LocalDynamicPreview(
        response,
        previewCountries(),
        initialState.mapValues { (_, value) -> DynamicValue.StringValue(value) } +
            numericInitialState.mapValues { (_, value) -> DynamicValue.NumberValue(value) },
        resendFailureMode,
        networkFailure = networkFailure?.let { label ->
            AppFailure.Category.entries.firstOrNull { it.name.equals(label, ignoreCase = true) }
                ?: error("Unsupported preview network failure category")
        },
    )
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

/** Development-only Bootstrap outcome for real SplashStore/BootstrapContent rendering. */
data class LocalBootstrapPreview(val outcome: Result<BootstrapOutput>)

fun parseLocalBootstrapPreview(raw: String): LocalBootstrapPreview {
    val json = Json { ignoreUnknownKeys = true }
    val dto = json.decodeFromString<BootstrapResponseDto>(raw)
    return LocalBootstrapPreview(BootstrapMapper().map(dto))
}

fun simulatedLocalBootstrapFailure(category: String): LocalBootstrapPreview {
    val kind = AppFailure.Category.entries.firstOrNull {
        it.name.equals(category, ignoreCase = true)
    } ?: error("Unsupported Bootstrap preview failure category")
    return LocalBootstrapPreview(Result.failure(AppFailure(kind)))
}
