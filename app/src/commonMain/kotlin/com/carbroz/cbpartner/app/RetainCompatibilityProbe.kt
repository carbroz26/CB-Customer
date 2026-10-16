package com.carbroz.cbpartner.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import kotlin.uuid.Uuid

/**
 * Opt-in host lifecycle experiment. Deliberately isolated from the Dynamic runtime.
 * Check the identity before and after Android rotation or Web host recreation.
 * Remove once the cross-platform retention contract is verified.
 */
@Composable
fun RetainCompatibilityProbe() {
    val retained = retain { Uuid.random().toString() }
    var count by remember { mutableStateOf(0) }
    DisposableEffect(retained) {
        println("CBPartnerRetention event=attached identity=$retained")
        onDispose { println("CBPartnerRetention event=detached identity=$retained") }
    }
    Column {
        Text("Retention proof (no backend / no Dynamic state)")
        Text("Retained identity: $retained")
        Text("Composition-local counter: $count")
        Button(onClick = { count++ }) { Text("Recompose") }
    }
}
