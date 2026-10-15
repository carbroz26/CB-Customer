package com.carbroz.cbpartner.feature.splash

import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.FeatureConfig
import com.carbroz.cbpartner.domain.model.bootstrap.MaintenanceConfig
import com.carbroz.cbpartner.domain.model.bootstrap.UpdateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SplashStoreTest {
    @Test
    fun loadTransitionsToLoadingAndSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val expected = sampleOutput()
        val store = SplashStore(
            bootstrap = object : ApplicationBootstrap {
                override suspend fun invoke(): Result<BootstrapOutput> = Result.success(expected)
            },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(SplashIntent.LoadBootstrap)
        assertEquals(SplashState.Loading, store.state.value)

        advanceUntilIdle()

        assertEquals(SplashState.Success(expected), store.state.value)
        store.close()
    }

    @Test
    fun failureCanBeRetried() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var calls = 0
        val expected = sampleOutput()
        val store = SplashStore(
            bootstrap = object : ApplicationBootstrap {
                override suspend fun invoke(): Result<BootstrapOutput> {
                    calls++
                    return if (calls == 1) {
                        Result.failure(IllegalStateException("Bootstrap failed"))
                    } else {
                        Result.success(expected)
                    }
                }
            },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()
        assertIs<SplashState.Failure>(store.state.value)

        store.accept(SplashIntent.RetryBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.Success(expected), store.state.value)
        assertEquals(2, calls)
        store.close()
    }


    @Test
    fun maintenancePreventsReadyEvenWhenUpdateRequired() = runTest {
        val output = sampleOutput().copy(
            maintenance = MaintenanceConfig(true, "Scheduled maintenance", "Try later"),
            update = sampleOutput().update.copy(required = true, storeUrl = "https://example.test/store"),
        )
        val store = SplashStore(
            bootstrap = object : ApplicationBootstrap {
                override suspend fun invoke(): Result<BootstrapOutput> = Result.success(output)
            },
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.Maintenance("Scheduled maintenance", "Try later"), store.state.value)
        store.close()
    }

    @Test
    fun requiredUpdateBlocksReadyWhenMaintenanceDisabled() = runTest {
        val output = sampleOutput().copy(
            update = sampleOutput().update.copy(required = true, storeUrl = "https://example.test/store"),
        )
        val store = SplashStore(
            bootstrap = object : ApplicationBootstrap {
                override suspend fun invoke(): Result<BootstrapOutput> = Result.success(output)
            },
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.RequiredUpdate("https://example.test/store"), store.state.value)
        store.close()
    }

    @Test
    fun optionalUpdateDoesNotBlockReady() = runTest {
        val output = sampleOutput().copy(
            update = sampleOutput().update.copy(optional = true),
        )
        val store = SplashStore(
            bootstrap = object : ApplicationBootstrap {
                override suspend fun invoke(): Result<BootstrapOutput> = Result.success(output)
            },
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.Success(output), store.state.value)
        store.close()
    }

    private fun sampleOutput() = BootstrapOutput(
        version = "1",
        maintenance = MaintenanceConfig(false, null, null),
        update = UpdateConfig(false, false, "1.0.0", "1.0.0", null),
        features = FeatureConfig(true, true, true),
        authenticated = false,
        destination = DynamicDestination(
            screenId = "partner_login",
            templateId = "tpl_P101L",
            templateType = "form_template",
            endpoint = "/api/v1/partner/screen/auth_login",
        ),
    )
}
