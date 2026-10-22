package com.carbroz.cbpartner.domain.model.dynamic.section

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Read-only source of immutable list records by backend-authored catalog key.
 * Catalog contents are product data; filtering and selection belong to the Dynamic runtime.
 * Null denotes an unavailable/unknown catalog, never an empty result.
 */
fun interface DynamicListCatalog {
    fun items(key: String): List<Map<String, DynamicValue>>?
}
