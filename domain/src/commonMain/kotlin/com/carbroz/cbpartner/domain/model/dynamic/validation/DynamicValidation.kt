package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
)
