package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class DynamicActionExecutor(private val handlers: List<DynamicActionHandler>) {
    private val handlersByType = handlers.associateBy { it.type.lowercase() }
    suspend fun execute(action: DynamicAction, navigation: DynamicNavigationState, bindings: Map<String,String>, context: Map<String,String>, previousResponse: DynamicValue?): Result<DynamicActionResult> = runCatching {
        if (action.type.equals("sequence", true)) executeSequence(action, navigation, bindings, context, previousResponse)
        else dispatch(action, navigation, bindings, context, previousResponse)
    }
    private suspend fun dispatch(action: DynamicAction, navigation: DynamicNavigationState, bindings: Map<String,String>, context: Map<String,String>, previousResponse: DynamicValue?): DynamicActionResult {
        val normalizedType = action.type.lowercase()
        val handler = handlersByType[if (normalizedType == "back") "dismiss" else normalizedType] ?: error("Unsupported dynamic action: " + action.type)
        return handler.handle(action, navigation, bindings, context, previousResponse)
    }
    private suspend fun executeSequence(action: DynamicAction, navigation: DynamicNavigationState, bindings: Map<String,String>, context: Map<String,String>, previousResponse: DynamicValue?): DynamicActionResult {
        val actions = action.payload["actions"] as? DynamicValue.ArrayValue ?: error("Dynamic sequence actions are missing")
        var last: DynamicActionResult = DynamicActionResult.Completed
        actions.value.forEach { value ->
            val objectValue = value as? DynamicValue.ObjectValue ?: error("Dynamic sequence action is invalid")
            val type = (objectValue.value["type"] as? DynamicValue.StringValue)?.value ?: error("Dynamic sequence action type is missing")
            val payload = (objectValue.value["payload"] as? DynamicValue.ObjectValue)?.value ?: emptyMap()
            val destination = (objectValue.value["destination"] as? DynamicValue.ObjectValue)?.toDestination()
            last = execute(DynamicAction(type, payload, destination), navigation, bindings, context, previousResponse).getOrThrow()
        }
        return last
    }
    private fun DynamicValue.ObjectValue.toDestination(): DynamicDestination? {
        fun string(key: String) = (value[key] as? DynamicValue.StringValue)?.value
        val screenId = string("screenId") ?: return null
        val templateId = string("templateId") ?: return null
        val templateType = string("templateType") ?: return null
        val endpoint = string("endpoint") ?: return null
        return DynamicDestination(screenId, templateId, templateType, endpoint)
    }
}