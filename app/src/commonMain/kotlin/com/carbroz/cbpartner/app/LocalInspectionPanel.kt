package com.carbroz.cbpartner.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays developer-only local inspection access status.
 *
 * This shell never receives, captures, or renders backend data. Actual inspection
 * remains unavailable until the independent backend trust gate is verified and
 * a separately approved sanitized inspection source exists.
 */
@Composable
internal fun LocalInspectionPanel(
    optedIn: Boolean,
    inspectionEligible: Boolean,
    onOptIn: () -> Unit,
    onClose: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CB-PARTNER / LOCAL SDUI INSPECTOR")
            Text("Development Debug")
            Text(if (optedIn) "Developer opt-in: enabled" else "Developer opt-in: off")
            Text(if (inspectionEligible) "Backend verification: passed" else "Backend verification: unavailable")
            Text("Sanitized SDUI snapshots are not implemented yet")
            if (!optedIn) {
                Button(onClick = onOptIn) { Text("Enable local inspector") }
            }
            TextButton(onClick = onClose) { Text("Close") }
        }
    }
}
