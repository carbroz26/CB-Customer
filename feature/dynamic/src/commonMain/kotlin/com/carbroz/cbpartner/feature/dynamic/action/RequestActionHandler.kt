package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class RequestActionHandler(
    private val repository: DynamicRepository,
) : DynamicActionHandler {
    override val type: String = "request"

    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        val endpoint = DynamicActionValueResolver.string(action.payload["endpoint"])
            ?: error("Dynamic request endpoint is missing")
        val method = DynamicActionValueResolver.string(action.payload["method"]) ?: "POST"
        val authentication = DynamicActionValueResolver.string(action.payload["authentication"]) ?: "NONE"
        val body = action.payload["body"]?.let {
            DynamicActionValueResolver.resolve(it, bindings, context, previousResponse)
        }

        val response = repository.fetch(
            destination = DynamicDestination(
                screenId = "action:$endpoint",
                templateId = "action",
                templateType = "request",
                endpoint = endpoint,
            ),
            body = body,
            method = method,
            authentication = authentication,
        ).getOrThrow()

        if (DynamicActionValueResolver.string(action.payload["responseMode"])?.lowercase() == "none") {
            return DynamicActionResult.Completed
        }

        val destination = response.destination
            ?: error("Dynamic request did not provide data.destination")
        when (DynamicActionValueResolver.string(action.payload["mode"])?.lowercase()) {
            "push", "add" -> navigation.push(destination)
            else -> navigation.replace(destination)
        }
        return DynamicActionResult.Navigate(destination, response)
    }
}
