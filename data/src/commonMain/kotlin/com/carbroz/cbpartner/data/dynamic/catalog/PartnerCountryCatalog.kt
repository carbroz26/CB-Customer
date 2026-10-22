package com.carbroz.cbpartner.data.dynamic.catalog

import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Product-country catalog adapter. No countries are fabricated while the vetted,
 * backend-supported dataset is unavailable. A supplied snapshot is never mutated.
 */
class PartnerCountryCatalog(
    private val approvedCountries: List<Map<String, DynamicValue>>? = null,
) : DynamicListCatalog {
    override fun items(key: String): List<Map<String, DynamicValue>>? =
        if (key == "countries") approvedCountries?.map { it.toMap() } else null
}
