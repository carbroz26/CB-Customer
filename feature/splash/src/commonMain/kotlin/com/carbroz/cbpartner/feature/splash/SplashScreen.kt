package com.carbroz.cbpartner.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.feature.splash.generated.resources.Res
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_app_name
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_brand_logo
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_failure_retry
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_hero_image
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_loading_subtitle
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_loading_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_maintenance_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_maintenance_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_required_update_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_required_update_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_service_subtitle
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_update_action
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_update_unavailable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Displays the native Compose startup artwork and Store-owned bootstrap states.
 * Bootstrap requests and the successful Dynamic handoff stay with the existing Store and app host.
 */
@Composable
fun SplashScreen(
    store: SplashStore,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()
    var splashReported by remember(store) { mutableStateOf(false) }
    SideEffect {
        if (!splashReported) {
            splashReported = true
            runCatching {
                Logger.withTag("CBPartnerFlow").d {
                    "event=splash.committed sessionId=" +
                        safeDiagnosticId(store.diagnosticSessionId) +
                        " screen=splash"
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        store.accept(SplashIntent.LoadBootstrap)
    }

    CarBrozSplashTheme {
        SplashArtwork(
            state = state,
            modifier = modifier,
        )
    }
}

/** Owns splash presentation only; the image fills the viewport behind safe-area content. */
@Composable
private fun SplashArtwork(
    state: SplashState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(Res.drawable.splash_hero_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val compact = maxHeight < 700.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(0.28f))

                Image(
                    painter = painterResource(Res.drawable.splash_brand_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(if (compact) 82.dp else 94.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.splash_app_name),
                    fontSize = if (compact) 34.sp else 38.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.splash_service_subtitle),
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.weight(1f))

                SplashBottomStatus(
                    state = state,
                )

                Spacer(modifier = Modifier.height(if (compact) 20.dp else 32.dp))
            }
        }
    }
}

/** Indeterminate progress reflects an active bootstrap request, never a fabricated percentage. */
@Composable
private fun SplashBottomStatus(
    state: SplashState,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (state) {
            SplashState.Initial,
            SplashState.Loading,
            is SplashState.Success -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(Res.string.splash_loading_title),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.splash_loading_subtitle),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is SplashState.Failure,
            is SplashState.Maintenance,
            is SplashState.RequiredUpdate,
            is SplashState.OptionalUpdate -> Unit
        }
    }
}

// Until backend migration approves more destinations, only the canonical Android listing
// is permitted. No redirects, alternate schemes, arbitrary query parameters or userinfo.
fun isTrustedUpdateUrl(url: String): Boolean =
    url == "https://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android"
