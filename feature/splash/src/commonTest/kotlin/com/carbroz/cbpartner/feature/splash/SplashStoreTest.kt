package com.carbroz.cbpartner.feature.splash

import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapConfig
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.FeatureConfig
import com.carbroz.cbpartner.domain.model.bootstrap.MaintenanceConfig
import com.carbroz.cbpartner.domain.model.bootstrap.StartupConfig
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

    private fun sampleOutput() = BootstrapOutput(
        config = BootstrapConfig(
            version = "1",
            maintenance = MaintenanceConfig(false, null, null),
            update = UpdateConfig(false, false, "1.0.0", "1.0.0", null),
            features = FeatureConfig(true, true, true),
        ),
        startup = StartupConfig(
            authenticated = false,
            destination = DynamicDestination(
                screenId = "partner_login",
                templateId = "tpl_P101L",
                templateType = "form_template",
                endpoint = "/api/v1/partner/screen/auth_login",
            ),
        ),
    )
}
