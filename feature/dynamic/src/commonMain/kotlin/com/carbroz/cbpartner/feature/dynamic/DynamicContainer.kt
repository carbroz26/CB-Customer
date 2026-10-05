package com.carbroz.cbpartner.feature.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer
import com.carbroz.cbpartner.feature.dynamic.store.DynamicIntent
import com.carbroz.cbpartner.feature.dynamic.store.DynamicState
import com.carbroz.cbpartner.feature.dynamic.store.DynamicStore
import com.carbroz.cbpartner.navigation.DynamicNavigationState

@Composable
fun DynamicContainer(
    destination: DynamicDestination,
    navigation: DynamicNavigationState,
    store: DynamicStore,
    registry: DynamicRegistry,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()
    val bindings by store.bindings.collectAsState()
    val context by store.context.collectAsState()

    LaunchedEffect(destination) {
        store.updateDestination(destination)
        store.accept(DynamicIntent.Load)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            DynamicState.Initial, DynamicState.Loading -> CircularProgressIndicator()
            is DynamicState.Success -> DynamicRenderer(
                registry = registry,
                bindings = bindings,
                contextValues = context,
                onAction = { store.accept(DynamicIntent.Action(it, navigation)) },
                onValueChange = { key, value -> store.accept(DynamicIntent.ValueChanged(key, value)) },
            ).Render(current.response, destination)
            is DynamicState.Failure -> Text(current.message)
        }
    }
}
