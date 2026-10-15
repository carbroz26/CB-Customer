package com.carbroz.cbpartner.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.feature.splash.generated.resources.Res
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_app_name
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_brand_partner
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_car_wash
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_failure_retry
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_loading_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_loading_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_maintenance_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_maintenance_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_required_update_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_update_action
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_update_unavailable
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_required_update_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_ready_description
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_ready_title
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_service_subtitle
import com.carbroz.cbpartner.feature.splash.generated.resources.splash_service_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(
    store: SplashStore,
    modifier: Modifier = Modifier,
) {
    val state by store.state.collectAsState()

    LaunchedEffect(Unit) {
        store.accept(SplashIntent.LoadBootstrap)
    }

    // The launch URL is approved for this platform only when it is an exact official listing.
    val uriHandler = LocalUriHandler.current
    val requiredUpdate = state as? SplashState.RequiredUpdate
    val trustedUrl = requiredUpdate?.storeUrl?.takeIf(::isTrustedUpdateUrl)
    var openFailed by remember(requiredUpdate) { mutableStateOf(false) }
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
                    Button(onClick = {
                        try {
                            uriHandler.openUri(trustedUrl)
                            openFailed = false
                        } catch (_: Exception) {
                            openFailed = true
                        }
                    }) {
                        Text(stringResource(Res.string.splash_update_action))
                    }
                }
            },
        )
    }

    CarBrozSplashTheme {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SplashDecorations()

            val compactHeight = maxHeight < 680.dp
            val illustrationSize = when {
                maxWidth < 420.dp -> maxWidth * 0.88f
                maxWidth < 700.dp -> 340.dp
                else -> 390.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = if (compactHeight) 20.dp else 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandHeader()

                Spacer(modifier = Modifier.weight(if (compactHeight) 0.35f else 0.65f))

                Image(
                    painter = painterResource(Res.drawable.splash_car_wash),
                    contentDescription = stringResource(Res.string.splash_service_title),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(illustrationSize),
                )

                Spacer(modifier = Modifier.height(if (compactHeight) 4.dp else 10.dp))

                Text(
                    text = when (val current = state) {
                        is SplashState.Success -> stringResource(Res.string.splash_ready_title)
                        is SplashState.Maintenance -> current.title?.takeIf(String::isNotBlank)
                            ?: stringResource(Res.string.splash_maintenance_title)
                        is SplashState.RequiredUpdate -> stringResource(Res.string.splash_required_update_title)
                        else -> stringResource(Res.string.splash_service_title)
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (val current = state) {
                        is SplashState.Success -> stringResource(Res.string.splash_ready_description)
                        is SplashState.Maintenance -> current.message?.takeIf(String::isNotBlank)
                            ?: stringResource(Res.string.splash_maintenance_description)
                        is SplashState.RequiredUpdate -> stringResource(Res.string.splash_required_update_description)
                        else -> stringResource(Res.string.splash_service_subtitle)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.92f),
                )

                Spacer(modifier = Modifier.height(if (compactHeight) 18.dp else 28.dp))

                SplashStatus(
                    state = state,
                    onRetry = { store.accept(SplashIntent.RetryBootstrap) },
                )

                Spacer(modifier = Modifier.weight(if (compactHeight) 0.4f else 0.7f))

                Text(
                    text = stringResource(Res.string.splash_app_name),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.splash_brand_partner),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.splash_car_wash),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = stringResource(Res.string.splash_app_name),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.splash_brand_partner),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.8f,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SplashStatus(
    state: SplashState,
    onRetry: () -> Unit,
) {
    when (state) {
        SplashState.Initial,
        SplashState.Loading,
        -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.88f),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(17.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        text = stringResource(Res.string.splash_loading_title),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = stringResource(Res.string.splash_loading_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        is SplashState.Success -> {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    text = stringResource(Res.string.splash_ready_title),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        is SplashState.Maintenance, is SplashState.RequiredUpdate -> Unit

        is SplashState.Failure -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                Button(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = onRetry,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(stringResource(Res.string.splash_failure_retry))
                }
            }
        }
    }
}

@Composable
private fun SplashDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-100).dp, y = (-80).dp)
                .alpha(0.55f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        )
        Box(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 70.dp)
                .alpha(0.65f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-38).dp, y = 118.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterStart)
                .offset(x = 24.dp, y = (-34).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
        )
    }
}

// Until backend migration approves more destinations, only the canonical Android listing
// is permitted. No redirects, alternate schemes, arbitrary query parameters or userinfo.
internal fun isTrustedUpdateUrl(url: String): Boolean =
    url == "https://play.google.com/store/apps/details?id=com.carbroz.cbpartner.android"
