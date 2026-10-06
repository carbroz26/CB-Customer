package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class RequestActionHandler(private val repository: DynamicRepository) : DynamicActionHandler {
    override val type = "request"
    override suspend fun handle(action: DynamicAction, navigation: DynamicNavigationState, bindings: Map<String,String>, context: Map<String,String>, previousResponse: DynamicValue?): DynamicActionResult {
        val endpoint = DynamicActionValueResolver.string(action.payload["endpoint"]) ?: error("Dynamic request endpoint is missing")
        val method = DynamicActionValueResolver.string(action.payload["method"]) ?: "POST"
        val authentication = DynamicActionValueResolver.string(action.payload["authentication"]) ?: "NONE"
        val validate = DynamicActionValueResolver.boolean(action.payload["validate"]) ?: false
        val responseMode = DynamicActionValueResolver.string(action.payload["responseMode"]) ?: "destination"
        val body = action.payload["body"]?.let { DynamicActionValueResolver.resolve(it, bindings, context, previousResponse) }
        val response = repository.fetch(DynamicRequest(endpoint = endpoint, method = method, authentication = authentication, validate = validate, body = body, responseMode = responseMode)).getOrThrow()
        if (responseMode.equals("none", true)) return DynamicActionResult.Completed
        val destination = response.destination ?: error("Dynamic request did not provide data.destination")
        when (DynamicActionValueResolver.string(action.payload["mode"])?.lowercase()) { "push", "add" -> navigation.push(destination); else -> navigation.replace(destination) }
        val contextChanges = buildMap {
            val phone = (body as? DynamicValue.ObjectValue)?.value?.get("phoneNumber")
            if (phone is DynamicValue.StringValue && phone.value.isNotBlank()) put("authFlow.phoneNumber", phone.value)

            val challengeId = (response.dataPayload as? DynamicValue.ObjectValue)?.value?.get("challengeId")
            if (challengeId is DynamicValue.StringValue && challengeId.value.isNotBlank()) {
                put("challengeId", challengeId.value)
            }
        }
        return DynamicActionResult.Navigate(destination, response, contextChanges)
    }
}