package com.carbroz.cbpartner.feature.dynamic.sdui

import co.touchlab.kermit.Logger
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

/** Native click and long-click dispatch; no gesture handler for absent actions. */
@OptIn(ExperimentalFoundationApi::class)
internal fun Modifier.dynamicGestures(
    actions: Map<String, DynamicAction>,
    dispatch: (DynamicAction) -> Unit,
    elementId: String? = null,
): Modifier {
    val click = actions["onClick"]
    val longClick = actions["onLongClick"]
    fun fire(event: String, action: DynamicAction) {
        reportDynamicOrigin(elementId, event, action.type)
        dispatch(action)
    }
    return when {
        longClick != null -> combinedClickable(
            onClick = { click?.let { fire("onClick", it) } },
            onLongClick = { fire("onLongClick", longClick) },
        )
        click != null -> clickable { fire("onClick", click) }
        else -> this
    }
}

/** Reports only validated element identity and registered action vocabulary, never payload values. */
internal fun reportDynamicOrigin(elementId: String?, event: String, actionType: String) {
    val safeId = elementId?.takeIf { it.matches(Regex("^[A-Za-z][A-Za-z0-9_]{0,63}$")) } ?: "redacted"
    val safeEvent = when (event) {
        "onClick", "onLongClick", "onValueChange", "onFocus", "onSubmit", "onSpanClick" -> event
        else -> "other"
    }
    val safeType = when (actionType.trim().lowercase()) {
        "request", "navigate", "present", "dismiss", "external_uri", "state", "sequence", "back" -> actionType.trim().lowercase()
        else -> "other"
    }
    runCatching {
        Logger.withTag("CBPartnerFlow").d { "event=element.action origin=$safeId trigger=$safeEvent type=$safeType" }
    }
}
