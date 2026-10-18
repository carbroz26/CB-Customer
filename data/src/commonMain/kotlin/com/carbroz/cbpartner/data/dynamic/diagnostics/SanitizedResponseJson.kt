package com.carbroz.cbpartner.data.dynamic.diagnostics

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

private val prettyTraceJson = Json { prettyPrint = true }

/**
 * Preserves a response's object/array shape while withholding unsafe values from Logcat.
 * Never emits raw response strings. Unknown keys are represented by a generic placeholder.
 * This projection is for diagnostics only, and never participates in screen rendering.
 */
internal fun sanitizedResponseJson(source: String): List<String> {
    val tree = Json.parseToJsonElement(source)
    var fields = 0
    val maxFields = 1500
    val allowedKeys = setOf(
        "status", "code", "message", "data", "traceId", "config", "version",
        "maintenance", "enabled", "title", "update", "required", "optional",
        "minimumVersion", "latestVersion", "storeUrl", "features", "startup",
        "authenticated", "destination", "screenId", "templateId", "templateType",
        "endpoint", "method", "authentication", "schemaVersion", "targetApp",
        "template", "id", "type", "properties", "components", "sections", "groups",
        "elements", "header", "footer", "leading", "trailing", "actions",
        "onClick", "onLongClick", "onFocus", "onSubmit", "onValueChange",
        "payload", "body", "binding", "validation", "key", "pattern", "validate",
        "responseMode", "retain", "mode", "semanticRole", "orientation",
        "verticalArrangement", "horizontalArrangement", "horizontalAlignment",
        "verticalAlignment", "spacing", "padding", "start", "top", "end", "bottom",
        "fillMaxSize", "fillMaxWidth", "fillMaxHeight", "height", "width",
        "maxHeight", "maxWidth", "fontSize", "fontWeight", "letterSpacing",
        "lineHeight", "textAlign", "color", "background", "border", "shape",
        "cornerRadius", "thickness", "contentScale", "weight", "keyboardType",
        "maxLength", "presentation", "count", "segmentWidth", "segmentHeight",
        "focusBorder", "angle", "colors", "stop", "underline", "spans", "text",
        "url", "fallbackUrl", "name", "size", "fillMaxWidth", "response",
        "\$binding", "\$context", "\$state", "\$literal",
    )
    val safeIdentityKeys = setOf("screenId", "templateId", "id", "key")
    val safeEnums = setOf(
        "SUCCESS", "PARTNER", "ANDROID", "NONE", "BEARER", "GET", "POST", "PUT",
        "PATCH", "DELETE", "destination", "none", "push", "replace", "pop",
        "form_template", "stack_component", "stack_section", "stack_group",
        "button", "text", "input", "image", "icon", "divider", "request",
        "navigate", "present", "dismiss", "external_uri", "state", "sequence",
        "vertical", "horizontal", "center", "start", "end", "left", "right",
        "top", "bottom", "spacedBy", "fit", "crop", "fillBounds", "number",
        "phone", "segmented", "roundedCorner", "linearGradient", "linear",
        "light", "dark", "transparent", "form", "action",
    )
    val numericStyleKeys = setOf(
        "status", "fontSize", "fontWeight", "letterSpacing", "lineHeight", "width",
        "height", "maxHeight", "maxWidth", "spacing", "start", "top", "end",
        "bottom", "cornerRadius", "thickness", "weight", "maxLength", "count",
        "segmentWidth", "segmentHeight", "angle", "stop",
    )
    val booleanKeys = setOf(
        "enabled", "required", "optional", "authenticated", "validate",
        "fillMaxSize", "fillMaxWidth", "fillMaxHeight", "underline",
        "registrationEnabled", "individualPartnerEnabled", "organizationPartnerEnabled",
    )
    val identityPattern = Regex("^[A-Za-z][A-Za-z0-9_]{0,63}$")
    fun safeString(key: String, value: String): String {
        return when {
            key in safeIdentityKeys && identityPattern.matches(value) -> value
            key == "type" && value in safeEnums -> value
            key in setOf("templateType", "method", "authentication", "orientation", "textAlign",
                "contentScale", "keyboardType", "responseMode", "mode", "targetApp", "theme",
                "statusBar", "semanticRole") && value in safeEnums -> value
            key in setOf("version", "schemaVersion", "minimumVersion", "latestVersion") &&
                value.matches(Regex("^[0-9]{1,4}(\\.[0-9]{1,4}){0,3}$")) -> value
            else -> "[redacted]"
        }
    }
    fun walk(value: JsonElement, key: String, depth: Int): JsonElement {
        if (++fields > maxFields || depth > 32) return JsonPrimitive("[truncated]")
        return when (value) {
            is JsonObject -> JsonObject(value.entries.associate { (name, child) ->
                val safeName = if (name in allowedKeys || name in booleanKeys) name else "[unknown-field]"
                safeName to walk(child, name, depth + 1)
            })
            is JsonArray -> JsonArray(value.map { walk(it, key, depth + 1) })
            JsonNull -> JsonNull
            is JsonPrimitive -> when {
                value.isString -> JsonPrimitive(safeString(key, value.content))
                value.booleanOrNull != null && key in booleanKeys -> value
                value.doubleOrNull != null && key in numericStyleKeys -> value
                else -> JsonPrimitive("[redacted]")
            }
        }
    }
    val sanitized = walk(tree, "", 0)
    val pretty = prettyTraceJson.encodeToString(JsonElement.serializer(), sanitized)
    return pretty.lines().take(1800).map { line -> line.take(350) }
}
