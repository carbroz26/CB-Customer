package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import kotlinx.coroutines.CancellationException

class DynamicActionExecutor(
    handlers: List<DynamicActionHandler>,
) {
    private val handlersByType: Map<String, DynamicActionHandler> = buildMap {
        handlers.forEach { handler ->
            val key = handler.type.trim().lowercase()
            require(key.isNotEmpty()) { "Dynamic action handler type must not be blank" }
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
    ): Result<DynamicActionResult> = try {
        Result.success(executeInternal(action, bindings, context, state, depth = 0))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        Result.failure(failure)
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
        val initialState = state.toMap()
        val sequenceState = state.toMutableMap()
        val effects = mutableListOf<DynamicActionEffect>()

        actions.value.forEach { value ->
            val objectValue = value as? DynamicValue.ObjectValue
                ?: error("Dynamic sequence action is invalid")
            val type = (objectValue.value["type"] as? DynamicValue.StringValue)?.value
                ?: error("Dynamic sequence action type is missing")
            val payload = (objectValue.value["payload"] as? DynamicValue.ObjectValue)?.value ?: emptyMap()
            val destination = (objectValue.value["destination"] as? DynamicValue.ObjectValue)?.toDestination()

            val result = executeInternal(
                DynamicAction(type, payload, destination),
                bindings,
                context,
                sequenceState,
                depth + 1,
            )

            val changes = result.stateChanges
            if (changes.isNotEmpty()) {
                sequenceState.applyChanges(changes)
            }
            effects += result.effects
        }

        // Derive a canonical path delta from the actual final state. This preserves dotted
        // paths for nested updates while avoiding replay-order bugs for overlapping paths.
        val stateChanges = stateDelta(initialState, sequenceState)

        return when {
            effects.isNotEmpty() -> DynamicActionResult.Sequence(stateChanges, effects)
            stateChanges.isNotEmpty() -> DynamicActionResult.StateChanged(stateChanges)
            else -> DynamicActionResult.Completed
        }
    }

    private fun stateDelta(
        initialState: Map<String, DynamicValue>,
        finalState: Map<String, DynamicValue>,
    ): Map<String, DynamicValue> {
        val changes = linkedMapOf<String, DynamicValue>()
        finalState.forEach { (key, value) ->
            collectStateDelta(
                path = key,
                before = initialState[key],
                after = value,
                beforeExists = initialState.containsKey(key),
                changes = changes,
            )
        }
        return changes
    }

    private fun collectStateDelta(
        path: String,
        before: DynamicValue?,
        after: DynamicValue,
        beforeExists: Boolean,
        changes: MutableMap<String, DynamicValue>,
    ) {
        if (beforeExists && before == after) return

        val beforeObject = before as? DynamicValue.ObjectValue
        val afterObject = after as? DynamicValue.ObjectValue
        if (afterObject != null && (!beforeExists || beforeObject != null)) {
            // A changed key set must be replaced as one value: path deltas cannot express
            // deleting an object member, while replaying child paths alone would retain it.
            if (
                afterObject.value.isEmpty() ||
                (beforeObject != null && beforeObject.value.keys != afterObject.value.keys)
            ) {
                changes[path] = after
                return
            }
            afterObject.value.forEach { (childKey, childValue) ->
                collectStateDelta(
                    path = "$path.$childKey",
                    before = beforeObject?.value?.get(childKey),
                    after = childValue,
                    beforeExists = beforeObject?.value?.containsKey(childKey) == true,
                    changes = changes,
                )
            }
            return
        }

        changes[path] = after
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
