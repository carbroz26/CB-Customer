package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DynamicRuntimeState {
    private val _values = MutableStateFlow<Map<String, DynamicValue>>(emptyMap())
    val values: StateFlow<Map<String, DynamicValue>> = _values.asStateFlow()

    fun get(path: String): DynamicValue =
        DynamicReferenceResolver.resolvePath(_values.value, path)

    fun set(path: String, value: DynamicValue) {
        _values.value = setPath(_values.value, path, value)
    }

    fun setAll(values: Map<String, DynamicValue>) {
        values.forEach(::set)
    }

    private fun setPath(
        root: Map<String, DynamicValue>,
        path: String,
        value: DynamicValue,
    ): Map<String, DynamicValue> {
        val segments = path.split('.').filter(String::isNotBlank)
        if (segments.isEmpty()) return root
        if (segments.size == 1) return root + (segments.first() to value)

        val head = segments.first()
        val tail = segments.drop(1).joinToString(".")
        val existing = root[head] as? DynamicValue.ObjectValue
        val child = setPath(existing?.value.orEmpty(), tail, value)
        return root + (head to DynamicValue.ObjectValue(child))
    }
}
