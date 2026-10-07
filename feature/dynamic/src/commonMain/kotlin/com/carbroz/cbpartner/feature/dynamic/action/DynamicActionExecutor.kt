package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState

class DynamicActionExecutor(
    handlers: List<DynamicActionHandler>,
) {
    private val handlersByType: Map<String, DynamicActionHandler> = buildMap {
        handlers.forEach { handler ->
            val key = handler.type.trim().lowercase()
            check(put(key, handler) == null) {
                "Duplicate dynamic action handler: " + handler.type
            }
        }
    }

    suspend fun execute(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Result<DynamicActionResult> = runCatching {
        executeInternal(action, bindings, context, state, depth = 0)
    }

    private suspend fun executeInternal(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        depth: Int,
    ): DynamicActionResult {
        check(depth < MAX_SEQUENCE_DEPTH) { "Dynamic action sequence depth exceeded" }
        return if (action.type.trim().equals("sequence", true)) {
            executeSequence(action, bindings, context, state, depth)
        } else {
            dispatch(action, bindings, context, state)
        }
    }

    private suspend fun dispatch(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult {
        val normalizedType = action.type.trim().lowercase()
        val handler = handlersByType[if (normalizedType == "back") "dismiss" else normalizedType]
            ?: error("Unsupported dynamic action: " + action.type)
        return handler.handle(action, bindings, context, state)
    }

    private suspend fun executeSequence(
        action: DynamicAction,
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
                bindings,
                context,
                sequenceState,
                depth + 1,
            )

            when (val result = last) {
                is DynamicActionResult.StateChanged -> sequenceState.applyChanges(result.changes)
                is DynamicActionResult.Navigate -> sequenceState.applyChanges(result.stateChanges)
                is DynamicActionResult.ExternalUri -> sequenceState.applyChanges(result.stateChanges)
                DynamicActionResult.Completed -> Unit
            }
        }

        val changes = sequenceState.filterKeys { key -> state[key] != sequenceState[key] }
        return when (last) {
            is DynamicActionResult.Navigate -> last.copy(stateChanges = changes)
            is DynamicActionResult.ExternalUri -> last.copy(stateChanges = changes)
            is DynamicActionResult.StateChanged -> DynamicActionResult.StateChanged(changes)
            DynamicActionResult.Completed ->
                if (changes.isEmpty()) DynamicActionResult.Completed
                else DynamicActionResult.StateChanged(changes)
        }
    }

    private fun MutableMap<String, DynamicValue>.applyChanges(changes: Map<String, DynamicValue>) {
        val updated = DynamicRuntimeState.applyChanges(this, changes)
        clear()
        putAll(updated)
    }

    private fun DynamicValue.ObjectValue.toDestination(): DynamicDestination? {
        fun string(key: String) = (value[key] as? DynamicValue.StringValue)?.value
        val screenId = string("screenId") ?: return null
        val templateId = string("templateId") ?: return null
        val templateType = string("templateType") ?: return null
        val endpoint = string("endpoint") ?: return null
        return DynamicDestination(screenId, templateId, templateType, endpoint)
    }

    private companion object {
        const val MAX_SEQUENCE_DEPTH = 32
    }
}
