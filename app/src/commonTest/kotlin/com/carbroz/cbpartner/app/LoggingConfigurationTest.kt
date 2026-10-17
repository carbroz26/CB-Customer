package com.carbroz.cbpartner.app

import co.touchlab.kermit.Severity
import com.carbroz.cbpartner.network.AppEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class LoggingConfigurationTest {
    @Test
    fun environmentSetsMinimumSeverityWithoutBuildType() {
        assertEquals(Severity.Debug, loggingSeverity(AppEnvironment.Development))
        assertEquals(Severity.Info, loggingSeverity(AppEnvironment.Staging))
        assertEquals(Severity.Warn, loggingSeverity(AppEnvironment.Production))
    }
}
