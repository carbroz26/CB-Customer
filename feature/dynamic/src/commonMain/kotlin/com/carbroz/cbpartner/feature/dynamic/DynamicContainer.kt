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
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
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
    runtimeState: DynamicRuntimeState,
    contextValues: Map<String, DynamicValue>,
    storeFactory: () -> DynamicStore,
    registry: DynamicRegistry,
    modifier: Modifier = Modifier,
) {
    val store = remember(entry) { storeFactory() }
    val state by store.state.collectAsState()
    val runtimeValues by runtimeState.values.collectAsState()
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
                is DynamicEffect.Navigation -> applyNavigation(effect.result, navigation)
                null -> Unit
            }
            if (effect != null) store.clearEffect()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            DynamicState.Initial, DynamicState.Loading -> CircularProgressIndicator()
            is DynamicState.Success -> {
                val response = current.screen.response
                if (response != null) {
                    DynamicRenderer(
                        registry = registry,
                        bindings = current.screen.bindings,
                        contextValues = contextValues,
                        stateValues = runtimeValues,
                        onAction = { store.accept(DynamicIntent.Action(it)) },
                        onValueChange = { key, value ->
                            store.accept(DynamicIntent.ValueChanged(key, value))
                        },
                    ).Render(response, entry.destination)
                }
            }
            is DynamicState.Failure -> Text(current.message)
        }
    }
}

private fun applyNavigation(
    result: DynamicActionResult.Navigate,
    navigation: DynamicNavigationState,
) {
    when (result.operation) {
        DynamicActionResult.NavigationOperation.Push ->
            result.destination?.let(navigation::push)
        DynamicActionResult.NavigationOperation.Replace ->
            result.destination?.let(navigation::replace)
        DynamicActionResult.NavigationOperation.Pop ->
            navigation.pop()
    }
}