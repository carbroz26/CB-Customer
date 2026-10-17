package com.carbroz.cbpartner.feature.dynamic.sdui.element

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
): Modifier {
    val click = actions["onClick"]
    val longClick = actions["onLongClick"]
    return when {
        longClick != null -> combinedClickable(
            onClick = { click?.let(dispatch) },
            onLongClick = { dispatch(longClick) },
        )
        click != null -> clickable { dispatch(click) }
        else -> this
    }
}
