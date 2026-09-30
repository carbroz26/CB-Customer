package com.carbroz.cbpartner.core

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeFoundationTest {
    @Test
    fun serializationRoundTripWorks() {
        val value = RuntimeProbe(id = 7, name = "cb-partner")
        val json = Json.encodeToString(value)
        val restored = Json.decodeFromString<RuntimeProbe>(json)

        assertEquals(value, restored)
    }

    @Test
    fun coroutinesTestDispatcherWorks() = runTest {
        assertEquals(0L, currentTime)
    }

    @Serializable
    private data class RuntimeProbe(
        val id: Int,
        val name: String,
    )
}
