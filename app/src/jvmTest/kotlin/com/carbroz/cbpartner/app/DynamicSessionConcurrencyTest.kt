package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * JVM contention regression for session commits.
 *
 * This does not prove Wasm, Kotlin/Native or host main-thread scheduling.
 * Barriers coordinate the start; no sleeps or external side effects.
 */
class DynamicSessionConcurrencyTest {
    @Test
    fun simultaneousCommitsAreAcceptedWithoutDroppingBatches() {
        val session = DynamicSession(
            diagnosticSessionId = "concurrent-session",
            initialDestination = DynamicDestination("root", "root-template", "form_template", "/root"),
        )
        val workers = 4
        val perWorker = 50
        val start = CountDownLatch(1)
        val ready = CountDownLatch(workers)
        val pool = Executors.newFixedThreadPool(workers)
        try {
            val results = (0 until workers).map { worker ->
                pool.submit<Boolean> {
                    ready.countDown()
                    if (!start.await(10, TimeUnit.SECONDS)) return@submit false
                    repeat(perWorker) { index ->
                        val effect = DynamicActionEffect.ExternalUri("https://example.invalid/${worker}-${index}")
                        if (!session.commit(emptyMap(), listOf(effect))) return@submit false
                    }
                    true
                }
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS), "Commit workers did not become ready")
            start.countDown()
            results.forEach { assertTrue(it.get(20, TimeUnit.SECONDS)) }
            val lease = session.attachConsumer()
            val observed = mutableSetOf<String>()
            while (true) {
                val claim = session.claimNext(lease) ?: break
                val effect = claim.effect as DynamicActionEffect.ExternalUri
                assertTrue(observed.add(effect.uri), "Duplicate committed effect")
                assertTrue(session.markDispatchStarted(claim))
                assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
            }
            assertEquals(workers * perWorker, observed.size)
        } finally {
            start.countDown()
            pool.shutdownNow()
        }
    }
}
