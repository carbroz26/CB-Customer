package com.carbroz.cbpartner.domain.model.dynamic.validation

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
)