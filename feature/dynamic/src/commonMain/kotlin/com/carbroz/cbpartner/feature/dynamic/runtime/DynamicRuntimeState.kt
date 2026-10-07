package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DynamicRuntimeState {
    private val _values = MutableStateFlow<Map<String, DynamicValue>>(emptyMap())
    val values: StateFlow<Map<String, DynamicValue>> = _values.asStateFlow()

    fun set(key: String, value: DynamicValue) {
        _values.value = _values.value + (key to value)
    }

    fun setAll(values: Map<String, DynamicValue>) {
        if (values.isNotEmpty()) _values.value = _values.value + values
    }
}
