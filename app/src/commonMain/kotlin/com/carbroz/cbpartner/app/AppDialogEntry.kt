package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import kotlinx.serialization.json.Json

/** Application-composition carrier only; neither a Store nor an action executor. */
internal data class AppDialogEntry(
    val presentation: DialogPresentation,
    val onAction: (DynamicAction) -> Unit,
    val onDismiss: () -> Unit,
)

/** The six offline-safe, backend-shaped fixtures are always mapped by the production mapper. */
internal suspend fun loadPackagedDialogTemplates(): Map<String, DialogPresentation> {
    val json = Json { ignoreUnknownKeys = true }
    val mapper = DynamicResponseMapper(json)
    return listOf(
        "maintenance", "required_update", "optional_update",
        "connection", "server_error", "secure_connection",
        "server_error_startup", "secure_connection_startup",
    ).associateWith { id ->
        val raw = Res.readBytes("files/dialog-v1-$id.json").decodeToString()
        val response = json.decodeFromString<DynamicResponseDto>(raw)
        requireNotNull(mapper.map(response).presentation as? DialogPresentation) {
            "Packaged dialog '$id' must remain a modal dialog"
        }
    }
}
