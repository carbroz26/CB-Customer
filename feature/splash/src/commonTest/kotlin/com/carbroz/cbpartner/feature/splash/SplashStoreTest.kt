package com.carbroz.cbpartner.feature.splash

import com.carbroz.cbpartner.domain.repository.BootstrapRepository
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashStoreTest {
    @Test
    fun loadTransitionsToLoadingAndSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val expected = sampleOutput()
        val store = SplashStore(
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> = Result.success(expected)
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
    fun rotationReattachLoadPreservesActiveBootstrapDialogUntilExplicitRetry() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var requests = 0
        val output = sampleOutput().copy(
            maintenance = MaintenanceConfig(true, "Maintenance", "Check back later"),
        )
        val store = SplashStore(
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> {
                    requests++
                    return Result.success(output)
                }
            },
            scope = CoroutineScope(dispatcher),
        )
        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()
        val active = assertIs<SplashState.Maintenance>(store.state.value)

        // Re-attaching SplashScreen after an Android configuration change invokes Load.
        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()
        assertEquals(active, store.state.value)
        assertEquals(1, requests)

        // An explicit user action is still allowed to refetch the startup policy.
        store.accept(SplashIntent.RetryBootstrap)
        advanceUntilIdle()
        assertEquals(2, requests)
        store.close()
    }

    @Test
    fun failureCanBeRetried() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var calls = 0
        val expected = sampleOutput()
        val store = SplashStore(
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> {
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
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> = Result.success(output)
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
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> = Result.success(output)
            },
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.RequiredUpdate("https://example.test/store", "1.0.0"), store.state.value)
        store.close()
    }

    @Test
    fun optionalUpdateOffersNotNowWithoutAnotherBootstrapRequest() = runTest {
        val output = sampleOutput().copy(
            update = sampleOutput().update.copy(optional = true),
        )
        val store = SplashStore(
            bootstrap = object : BootstrapRepository {
                override suspend fun fetch(): Result<BootstrapOutput> = Result.success(output)
            },
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )

        store.accept(SplashIntent.LoadBootstrap)
        advanceUntilIdle()

        assertEquals(SplashState.OptionalUpdate(output), store.state.value)
        store.accept(SplashIntent.SkipOptionalUpdate)
        assertEquals(SplashState.Success(output), store.state.value)
        store.close()
    }

    @Test
    fun bootstrapFailureDoesNotExposeServerOrExceptionDetails() = runTest {
        val failures = listOf(
            BootstrapFailure.Api("REJECTED", "Token secret and server path /internal/auth"),
            BootstrapFailure.Serialization("Malformed response includes sensitive payload"),
            IllegalStateException("https://api.example.test/private?token=secret"),
        )
        failures.forEach { failure ->
            val store = SplashStore(
                bootstrap = object : BootstrapRepository {
                    override suspend fun fetch(): Result<BootstrapOutput> = Result.failure(failure)
                },
                scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
            )

            store.accept(SplashIntent.LoadBootstrap)
            advanceUntilIdle()

            assertEquals(
                SplashState.Failure("Unable to start the application"),
                store.state.value,
            )
            store.close()
        }
    }


    @Test
    fun requiredUpdateLinkAcceptsOnlyOwnOfficialPlayStoreListing() {
        assertTrue(isTrustedUpdateUrl("https://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android"))
        listOf(
            "",
            "https://play.google.com/store/apps/details?id=com.other.app",
            "http://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android",
            "https://play.google.com.evil.test/store/apps/details?id=com.carbroz.cbpartner.android",
            "https://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android&redirect=evil",
            "javascript:alert(1)",
            "https://evil.test/",
        ).forEach { assertFalse(isTrustedUpdateUrl(it)) }
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
