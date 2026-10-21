package com.carbroz.cbpartner.feature.dynamic.runtime

/**
 * Explicit registry for named Dynamic validators.
 *
 * Duplicate registrations fail at construction time and unknown keys fail closed.
 */
class DynamicValidatorRegistry(
    validators: List<DynamicValidator>,
) {
    private val validatorsByKey = buildMap {
        validators.forEach { validator ->
            val key = validator.key.trim()
            require(key.isNotEmpty()) { "Dynamic validator key cannot be blank" }
            check(put(key, validator) == null) {
                "Duplicate Dynamic validator: " + key
            }
        }
    }

    fun requireValidator(key: String): DynamicValidator =
        validatorsByKey[key]
            ?: throw IllegalArgumentException("Unknown Dynamic validator: " + key)
}
