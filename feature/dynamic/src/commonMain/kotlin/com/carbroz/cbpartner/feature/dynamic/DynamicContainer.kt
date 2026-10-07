package com.carbroz.cbpartner.feature.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer
import com.carbroz.cbpartner.feature.dynamic.store.DynamicEffect
import com.carbroz.cbpartner.feature.dynamic.store.DynamicIntent
import com.carbroz.cbpartner.feature.dynamic.store.DynamicState
import com.carbroz.cbpartner.feature.dynamic.store.DynamicStore
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DynamicContainer(
    entry: DynamicNavigationEntry,
    navigation: DynamicNavigationState,
    storeFactory: () -> DynamicStore,
    registry: DynamicRegistry,
    modifier: Modifier = Modifier,
) {
    val store = remember(entry) { storeFactory() }
    val state by store.state.collectAsState()
    val bindings by store.bindings.collectAsState()
    val context by store.context.collectAsState()
    val runtimeValues by store.runtimeValues.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(store) {
        store.accept(DynamicIntent.Load)
    }

    DisposableEffect(store) {
        onDispose { store.close() }
    }

    LaunchedEffect(store) {
        store.effects.collectLatest { effect ->
            when (effect) {
                is DynamicEffect.ExternalUri -> uriHandler.openUri(effect.uri)
                null -> Unit
            }
            if (effect != null) store.clearEffect()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            DynamicState.Initial, DynamicState.Loading -> CircularProgressIndicator()
            is DynamicState.Success -> DynamicRenderer(
                registry = registry,
                bindings = bindings,
                contextValues = context,
                stateValues = runtimeValues,
                onAction = { store.accept(DynamicIntent.Action(it, navigation)) },
                onValueChange = { key, value -> store.accept(DynamicIntent.ValueChanged(key, value)) },
            ).Render(current.response, entry.destination)
            is DynamicState.Failure -> Text(current.message)
        }
    }
}
