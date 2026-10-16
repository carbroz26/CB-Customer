package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

/**
 * Compile-only proof that runtime-retain is available to commonMain targets.
 * Not invoked by production UI; delete after compatibility verification.
 */
@Composable
internal fun RetainCompatibilityProbe(): Any = retain { Any() }
