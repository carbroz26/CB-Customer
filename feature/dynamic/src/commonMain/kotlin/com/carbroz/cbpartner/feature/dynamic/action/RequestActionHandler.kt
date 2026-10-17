package com.carbroz.cbpartner.feature.dynamic.action

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver

class RequestActionHandler(
    private val repository: DynamicRepository,
) : DynamicActionHandler {
    override val type = "request"
    private val diagnosticLogger = Logger.withTag("CBPartnerFlow")

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult = handle(action, bindings, context, state, null)

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        traceContext: TraceContext?,
    ): DynamicActionResult {
        val endpoint = resolveString(action.payload["endpoint"], bindings, context, state)
            ?: error("Dynamic request endpoint is missing")
        val method = resolveString(action.payload["method"], bindings, context, state) ?: "POST"
        val authentication = resolveString(action.payload["authentication"], bindings, context, state) ?: "NONE"
        val validate = resolveBoolean(action.payload["validate"], bindings, context, state) ?: false
        val responseMode = if ("responseMode" in action.payload) {
            checkNotNull(resolveString(action.payload["responseMode"], bindings, context, state)) {
                "Invalid dynamic request responseMode"
            }.lowercase().also {
                check(it == "none" || it == "destination") { "Unsupported dynamic request responseMode" }
            }
        } else "none"
        val operation = if ("mode" in action.payload) {
            check(responseMode == "destination") { "Request mode requires destination responseMode" }
            when (resolveString(action.payload["mode"], bindings, context, state)?.lowercase()) {
                "push" -> DynamicActionResult.NavigationOperation.Push
                "replace" -> DynamicActionResult.NavigationOperation.Replace
                else -> error("Unsupported dynamic request navigation mode")
            }
        } else DynamicActionResult.NavigationOperation.Push
        if ("validate" in action.payload) {
            check(resolveBoolean(action.payload["validate"], bindings, context, state) != null) {
                "Invalid dynamic request validate flag"
            }
        }
        val body = action.payload["body"]?.let { DynamicReferenceResolver.resolve(it, bindings, context, state) }
        val retainedPaths = retainedPaths(action.payload["retain"])

        val response = repository.fetch(
            DynamicRequest(
                endpoint, method, authentication, validate, body, responseMode,
                sessionId = traceContext?.sessionId,
                actionId = traceContext?.actionId,
                parentActionId = traceContext?.parentActionId,
            ),
        ).getOrThrow()

        val stateChanges = extractRetainedState(retainedPaths, response.dataPayload)
        if (responseMode.equals("none", true)) {
            return if (stateChanges.isEmpty()) DynamicActionResult.Completed
            else DynamicActionResult.StateChanged(stateChanges)
        }

        val destination = response.destination ?: error("Dynamic request did not provide data.destination")
        runCatching {
            diagnosticLogger.i {
                "event=destination.resolved sessionId=${safeDiagnosticId(traceContext?.sessionId)} " +
                    "actionId=${safeDiagnosticId(traceContext?.actionId)} " +
                    "parentActionId=${safeDiagnosticId(traceContext?.parentActionId)} outcome=success"
            }
        }
        return DynamicActionResult.Navigate(destination, operation, stateChanges)
    }

    private fun resolve(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValue = value?.let { DynamicReferenceResolver.resolve(it, bindings, context, state) } ?: DynamicValue.NullValue

    private fun resolveString(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): String? = DynamicReferenceResolver.string(resolve(value, bindings, context, state))

    private fun resolveBoolean(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Boolean? = DynamicReferenceResolver.boolean(resolve(value, bindings, context, state))

    private fun retainedPaths(retain: DynamicValue?): Map<String, String> {
        if (retain == null) return emptyMap()
        val mappings = retain as? DynamicValue.ObjectValue
            ?: error("Dynamic request retain must be an object")
        return mappings.value.map { (key, pathValue) ->
            require(key.isNotBlank()) { "Dynamic request retain target is blank" }
            val path = DynamicReferenceResolver.string(pathValue)
            require(!path.isNullOrBlank()) { "Dynamic request retain path is invalid" }
            key to path
        }.toMap()
    }

    private fun extractRetainedState(
        mappings: Map<String, String>,
        dataPayload: DynamicValue?,
    ): Map<String, DynamicValue> {
        val root = mapOf("data" to (dataPayload ?: DynamicValue.NullValue))
        return mappings.map { (key, path) ->
            val value = DynamicReferenceResolver.resolveExistingPath(root, path)
                ?: error("Dynamic request retain path is missing")
            key to value
        }.toMap()
    }
}
