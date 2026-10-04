package com.carbroz.cbpartner.domain.model.bootstrap

import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination

data class StartupConfig(
    val authenticated: Boolean,
    val nextScreen: DynamicDestination,
)
