package com.carbroz.cbpartner.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid

/**
 * Opt-in G1 host lifecycle experiment only; it does not use app DI or Dynamic runtime.
 * Verify attach/detach is different from final retirement. Remove after proof.
 */
@Composable
fun RetainCompatibilityProbe() {
    var visible by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Retention Lifecycle Proof", style = MaterialTheme.typography.headlineSmall)
            if (visible) {
                RetainedProbeBody()
            } else {
                Text(
                    "Proof content removed. Check retirement log, then show it again.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Button(onClick = { visible = !visible }) {
                Text(if (visible) "Remove proof content" else "Show proof content")
            }
        }
    }
}

@Composable
private fun RetainedProbeBody() {
    val identity = retain { Uuid.random().toString() }
    var counter by remember { mutableStateOf(0) }

    DisposableEffect(identity) {
        println("CBPartnerRetention event=attached identity=$identity")
        onDispose { println("CBPartnerRetention event=detached identity=$identity") }
    }
    RetainedEffect(identity) {
        println("CBPartnerRetention event=retained identity=$identity")
        onRetire { println("CBPartnerRetention event=retired identity=$identity") }
    }

    Text("Retained identity: $identity", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    Text("Composition counter: $counter", style = MaterialTheme.typography.bodyLarge)
    Button(onClick = { counter++ }) { Text("Recompose") }
}
