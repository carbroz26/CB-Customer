package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class SequenceActionHandler : DynamicActionHandler {
    override val type: String = "sequence"

    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        val actions = action.payload["actions"] as? DynamicValue.ArrayValue
            ?: error("Dynamic sequence actions are missing")
        var last: DynamicActionResult = DynamicActionResult.Completed
        actions.value.forEach { value ->
            val objectValue = value as? DynamicValue.ObjectValue
                ?: error("Dynamic sequence action is invalid")
            val type = DynamicActionValueResolver.string(objectValue.value["type"])
                ?: error("Dynamic sequence action type is missing")
            last = when (type.lowercase()) {
                "navigate" -> NavigateActionHandler().handle(
                    DynamicAction(type, objectValue.value - "type"), navigation, bindings, context, previousResponse,
                )
                "request" -> error("Nested request actions must be dispatched by the parent executor")
                "external_uri" -> ExternalUriActionHandler().handle(
                    DynamicAction(type, objectValue.value - "type"), navigation, bindings, context, previousResponse,
                )
                "dismiss", "back" -> DismissActionHandler().handle(
                    DynamicAction(type, objectValue.value - "type"), navigation, bindings, context, previousResponse,
                )
                "present" -> PresentActionHandler().handle(
                    DynamicAction(type, objectValue.value - "type"), navigation, bindings, context, previousResponse,
                )
                "state" -> StateActionHandler().handle(
                    DynamicAction(type, objectValue.value - "type"), navigation, bindings, context, previousResponse,
                )
                else -> error("Unsupported sequence action: $type")
            }
        }
        return last
    }
}
