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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
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

    // Preserve the existing startup blocker: a required update cannot be dismissed.
    val uriHandler = LocalUriHandler.current
    val requiredUpdate = state as? SplashState.RequiredUpdate
    val trustedUrl = requiredUpdate?.storeUrl?.takeIf(::isTrustedUpdateUrl)
    var openFailed by remember(requiredUpdate) { mutableStateOf(false) }

    CarBrozSplashTheme {
        SplashArtwork(
            state = state,
            onRetry = { store.accept(SplashIntent.RetryBootstrap) },
            modifier = modifier,
        )

        if (requiredUpdate != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(Res.string.splash_required_update_title)) },
                text = {
                    Text(
                        stringResource(
                            if (trustedUrl == null || openFailed) {
                                Res.string.splash_update_unavailable
                            } else {
                                Res.string.splash_required_update_description
                            },
                        ),
                    )
                },
                confirmButton = {
                    if (trustedUrl != null) {
                        Button(
                            onClick = {
                                try {
                                    uriHandler.openUri(trustedUrl)
                                    openFailed = false
                                } catch (_: Exception) {
                                    openFailed = true
                                }
                            },
                        ) {
                            Text(stringResource(Res.string.splash_update_action))
                        }
                    }
                },
            )
        }
    }
}

/** Owns splash presentation only; the image fills the viewport behind safe-area content. */
@Composable
private fun SplashArtwork(
    state: SplashState,
    onRetry: () -> Unit,
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
                    modifier = Modifier.size(if (compact) 76.dp else 88.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.splash_app_name),
                    fontSize = if (compact) 32.sp else 36.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(Res.string.splash_service_subtitle),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.weight(1f))

                SplashBottomStatus(
                    state = state,
                    compact = compact,
                    onRetry = onRetry,
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
    compact: Boolean,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (state) {
            SplashState.Initial,
            SplashState.Loading,
            is SplashState.Success -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(if (compact) 184.dp else 220.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(Res.string.splash_loading_title),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is SplashState.Failure -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(Res.string.splash_failure_retry))
                }
            }

            is SplashState.Maintenance -> {
                Text(
                    text = state.title?.takeIf(String::isNotBlank)
                        ?: stringResource(Res.string.splash_maintenance_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.message?.takeIf(String::isNotBlank)
                        ?: stringResource(Res.string.splash_maintenance_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            is SplashState.RequiredUpdate -> Unit
        }
    }
}

// Until backend migration approves more destinations, only the canonical Android listing
// is permitted. No redirects, alternate schemes, arbitrary query parameters or userinfo.
internal fun isTrustedUpdateUrl(url: String): Boolean =
    url == "https://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android"
