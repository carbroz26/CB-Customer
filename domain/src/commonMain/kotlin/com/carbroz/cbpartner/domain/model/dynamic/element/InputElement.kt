package com.carbroz.cbpartner.domain.model.dynamic

data class InputElement(
    override val id: String,
    override val semanticRole: String? = null,
    val placeholder: String? = null,
    val keyboardType: String = "text",
    val maxLength: Int? = null,
    val weight: Double? = null,
    val validation: DynamicValidation? = null,
    val binding: DynamicBinding? = null,
    val presentation: DynamicInputPresentation? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
