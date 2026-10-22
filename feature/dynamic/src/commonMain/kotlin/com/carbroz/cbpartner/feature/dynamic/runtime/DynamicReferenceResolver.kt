package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

internal object DynamicReferenceResolver {
    private val namespaces = setOf(
        "\$binding",
        "\$context",
        "\$state",
        "\$literal",
        "\$item",
        "\$config",
    )

    private val legalPaths = setOf("legal.termsUrl", "legal.privacyUrl")

    fun resolve(
        value: DynamicValue,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        item: Map<String, DynamicValue>? = null,
        trustedConfig: Map<String, DynamicValue>? = null,
    ): DynamicValue = when (value) {
        is DynamicValue.ObjectValue -> {
            val unknownReferenceKeys = value.value.keys.filter { it.startsWith("\$") && it !in namespaces }
            check(unknownReferenceKeys.isEmpty()) {
                "Unsupported Dynamic reference namespace: " + unknownReferenceKeys.first()
            }

            val referenceKeys = value.value.keys.intersect(namespaces)
            check(referenceKeys.size <= 1) {
                "Dynamic value contains multiple reference namespaces"
            }

            val referenceKey = referenceKeys.singleOrNull()
            check(referenceKey == null || value.value.size == 1) {
                "Dynamic reference objects cannot contain additional properties"
            }

            when (referenceKey) {
                "\$binding" -> resolvePath(bindings, string(value.value.getValue(referenceKey)).orEmpty())
                "\$context" -> resolvePath(context, string(value.value.getValue(referenceKey)).orEmpty())
                "\$state" -> resolvePath(state, string(value.value.getValue(referenceKey)).orEmpty())
                "\$item" -> {
                    check(item != null) { "Dynamic item reference is outside a list row" }
                    val path = string(value.value.getValue(referenceKey)).orEmpty()
                    check(path.isNotBlank()) { "Dynamic item reference path is missing" }
                    resolveExistingPath(item, path)
                        ?: error("Dynamic item reference path does not exist")
                }
                "\$config" -> {
                    check(trustedConfig != null) { "Trusted Dynamic app config is unavailable" }
                    val path = string(value.value.getValue(referenceKey)).orEmpty()
                    check(path in legalPaths) { "Dynamic config path is not allowlisted" }
                    val uri = resolveExistingPath(trustedConfig, path)
                    check(uri is DynamicValue.StringValue && uri.value.isNotBlank()) {
                        "Dynamic config URI value is missing or invalid"
                    }
                    uri
                }
                "\$literal" -> resolve(
                    value.value.getValue(referenceKey),
                    bindings,
                    context,
                    state,
                    item,
                    trustedConfig,
                )
                null -> DynamicValue.ObjectValue(
                    value.value.mapValues { (_, child) ->
                        resolve(child, bindings, context, state, item, trustedConfig)
                    },
                )
                else -> error("Unsupported Dynamic reference namespace: $referenceKey")
            }
        }

        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(
            value.value.map { child -> resolve(child, bindings, context, state, item, trustedConfig) },
        )

        else -> value
    }

    fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value

    fun boolean(value: DynamicValue?): Boolean? =
        (value as? DynamicValue.BooleanValue)?.value

    fun resolveExistingPath(root: Map<String, DynamicValue>, path: String): DynamicValue? {
        val segments = path.split('.')
        if (segments.any(String::isBlank)) return null
        root[path]?.let { return it }
        var current: DynamicValue = root[segments.first()] ?: return null
        for (segment in segments.drop(1)) {
            current = (current as? DynamicValue.ObjectValue)?.value?.get(segment) ?: return null
        }
        return current
    }

    fun resolvePath(root: Map<String, DynamicValue>, path: String): DynamicValue {
        val segments = path.split('.').filter(String::isNotBlank)
        if (segments.isEmpty()) return DynamicValue.NullValue
        root[path]?.let { return it }

        var current: DynamicValue = root[segments.first()] ?: return DynamicValue.NullValue
        for (segment in segments.drop(1)) {
            current = when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> return DynamicValue.NullValue
            }
        }
        return current
    }
}
