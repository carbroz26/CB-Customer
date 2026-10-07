package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination

/**
 * Identity of one item in the Dynamic navigation back stack.
 *
 * Mutable Dynamic screen state is scoped to the NavEntry content in :feature:dynamic.
 */
class DynamicNavigationEntry(
    val destination: DynamicDestination,
)
