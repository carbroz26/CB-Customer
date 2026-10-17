package com.carbroz.cbpartner.app

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestConfig
import co.touchlab.kermit.TestLogWriter
import kotlin.test.assertTrue
import com.carbroz.cbpartner.network.AppEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalKermitApi::class)
class LoggingConfigurationTest {
    @Test
    fun environmentSetsMinimumSeverityWithoutBuildType() {
        assertEquals(Severity.Debug, loggingSeverity(AppEnvironment.Development))
        assertEquals(Severity.Info, loggingSeverity(AppEnvironment.Staging))
        assertEquals(Severity.Warn, loggingSeverity(AppEnvironment.Production))
    }
    @Test
    fun isolatedLoggerFiltersBelowEnvironmentThreshold() {
        for (environment in AppEnvironment.entries) {
            val writer = TestLogWriter(loggable = Severity.Verbose)
            val logger = Logger(
                TestConfig(minSeverity = loggingSeverity(environment), logWriterList = listOf(writer)),
                "CBPartnerFlow",
            )
            logger.d { "debug" }
            logger.i { "info" }
            logger.w { "warning" }
            val messages = writer.logs.map { it.message }
            assertEquals(environment == AppEnvironment.Development, "debug" in messages)
            assertEquals(environment != AppEnvironment.Production, "info" in messages)
            assertTrue("warning" in messages)
        }
    }
}
