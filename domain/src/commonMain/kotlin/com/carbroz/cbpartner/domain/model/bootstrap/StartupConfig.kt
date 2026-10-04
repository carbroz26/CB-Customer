package com.carbroz.cbpartner.domain.model.bootstrap

import com.carbroz.cbpartner.domain.model.DynamicDestination

data class StartupConfig(
    val authenticated: Boolean,
    val destination: DynamicDestination,
)
