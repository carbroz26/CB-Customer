package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class DynamicActionExecutor(
    handlers: List<DynamicActionHandler>,
) {
    private val handlersByType: Map<String, DynamicActionHandler> = buildMap {
        handlers.forEach { handler ->
            val key = handler.type.lowercase()
            check(put(key, handler) == null) { "Duplicate dynamic action handler: ${handler.type}" }
        }
    }

    suspend fun execute(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Result<DynamicActionResult> = runCatching {
        executeInternal(action, navigation, bindings, context, state, depth = 0)
    }

    private suspend fun executeInternal(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        depth: Int,
    ): DynamicActionResult {
        check(depth < MAX_SEQUENCE_DEPTH) { "Dynamic action sequence depth exceeded" }
        return if (action.type.equals("sequence", true)) {
            executeSequence(action, navigation, bindings, context, state, depth)
        } else {
            dispatch(action, navigation, bindings, context, state)
        }
    }

    private suspend fun dispatch(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult {
        val normalizedType = action.type.lowercase()
        val handler = handlersByType[if (normalizedType == "back") "dismiss" else normalizedType]
            ?: error("Unsupported dynamic action: ${action.type}")
        return handler.handle(action, navigation, bindings, context, state)
    }

    private suspend fun executeSequence(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        depth: Int,
    ): DynamicActionResult {
        val actions = action.payload["actions"] as? DynamicValue.ArrayValue
            ?: error("Dynamic sequence actions are missing")
        var last: DynamicActionResult = DynamicActionResult.Completed
        val sequenceState = state.toMutableMap()
        actions.value.forEach { value ->
            val objectValue = value as? DynamicValue.ObjectValue
                ?: error("Dynamic sequence action is invalid")
            val type = (objectValue.value["type"] as? DynamicValue.StringValue)?.value
                ?: error("Dynamic sequence action type is missing")
            val payload = (objectValue.value["payload"] as? DynamicValue.ObjectValue)?.value ?: emptyMap()
            val destination = (objectValue.value["destination"] as? DynamicValue.ObjectValue)?.toDestination()
            last = executeInternal(
                DynamicAction(type, payload, destination),
                navigation,
                bindings,
                context,
                sequenceState,
                depth + 1,
            )
            when (val result = last) {
                is DynamicActionResult.StateChanged -> sequenceState.putAll(result.changes)
                is DynamicActionResult.Navigate -> sequenceState.putAll(result.stateChanges)
                else -> Unit
            }
        }
        return when (last) {
            is DynamicActionResult.Navigate -> last.copy(stateChanges = sequenceState.filterKeys { key -> state[key] != sequenceState[key] })
            is DynamicActionResult.StateChanged -> DynamicActionResult.StateChanged(
                sequenceState.filterKeys { key -> state[key] != sequenceState[key] },
            )
            else -> last
        }
    }

    private fun DynamicValue.ObjectValue.toDestination(): DynamicDestination? {
        fun string(key: String) = (value[key] as? DynamicValue.StringValue)?.value
        val screenId = string("screenId") ?: return null
        val templateId = string("templateId") ?: return null
        val templateType = string("templateType") ?: return null
        val endpoint = string("endpoint") ?: return null
        return DynamicDestination(screenId, templateId, templateType, endpoint)
    }

    private companion object { const val MAX_SEQUENCE_DEPTH = 32 }
}
