package com.carbroz.cbpartner.web

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Disposable Web-only visual prototype for matching the owner's reference screenshots.
 *
 * This file is intentionally independent of production JSON, rendering, Store and networking.
 * Nothing in this preview is a production country-support policy or a design token default.
 * Use cbpartnerLoginPreview=1 to test the real Dynamic runtime instead.
 */
@Composable
internal fun VisualLoginPreview() {
    val countries = previewCountryRows
    var countryId by remember { mutableStateOf("IN") }
    var phone by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var sheetOpen by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var previewMessage by remember { mutableStateOf(false) }
    val country = countries.first { it.id == countryId }
    val phoneValid = country.id == "IN" && Regex("^[6-9][0-9]{9}$").matches(phone)

    MaterialTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF2F8FF), Color(0xFFDDEEFF), Color(0xFFC2DCEB)),
                    ),
                ),
        ) {
            val sheetHeight = maxHeight * 0.70f
            val contentWidth = maxWidth.coerceAtMost(430.dp)

            // Abstract placeholder — an image/photo has intentionally not been fabricated.
            PreviewSkyline(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight * 0.37f)
                    .align(Alignment.Center),
            )

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PreviewShield()
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "CB-Partner",
                        color = brandBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Welcome back",
                    color = brandBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Access your partner account",
                    color = Color(0xFF64748B),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .align(Alignment.BottomCenter)
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                        ),
                    )
                    .background(Color.White)
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                Text(
                    text = "Mobile number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF18263B),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            if (attempted && !phoneValid) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                            RoundedCornerShape(12.dp),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                search = ""
                                sheetOpen = true
                            }
                            .padding(start = 12.dp, end = 8.dp)
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PreviewFlag(country.id)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            country.dialCode,
                            color = Color(0xFF18263B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(5.dp))
                        PreviewChevron()
                    }
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(Color(0xFFD1D5DB)),
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        value = phone,
                        onValueChange = { input ->
                            phone = input.filter { it in '0'..'9' }.take(15)
                            attempted = false
                            previewMessage = false
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color(0xFF111827),
                        ),
                        decorationBox = { inner ->
                            Box {
                                if (phone.isEmpty()) Text(
                                    "Enter mobile number",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 16.sp,
                                )
                                inner()
                            }
                        },
                    )
                }
                if (attempted && !phoneValid) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Enter a valid mobile number",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1769D2))
                        .clickable {
                            attempted = true
                            previewMessage = phoneValid
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (previewMessage) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Visual preview only — no OTP request was sent",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                // The reference explicitly requires these lines to remain separate.
                Text(
                    text = "By continuing, you agree to our",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Terms & Conditions",
                        fontSize = 12.sp,
                        color = Color(0xFF0A3A8D),
                        textDecoration = TextDecoration.Underline,
                    )
                    Text(
                        "   |   ",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        "Privacy Policy",
                        fontSize = 12.sp,
                        color = Color(0xFF0A3A8D),
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }

            if (sheetOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.54f))
                        .clickable { sheetOpen = false },
                )
                Column(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(sheetHeight)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Spacer(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFD1D5DB)),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Select country code",
                            color = brandBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { sheetOpen = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("×", color = brandBlue, fontSize = 26.sp)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(46.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .border(
                                1.dp,
                                Color(0xFFD6E2F0),
                                RoundedCornerShape(9.dp),
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PreviewSearchIcon()
                        Spacer(Modifier.width(9.dp))
                        BasicTextField(
                            modifier = Modifier.weight(1f),
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF18263B)),
                            decorationBox = { inner ->
                                Box {
                                    if (search.isEmpty()) Text(
                                        "Search country or code...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                    )
                                    inner()
                                }
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val visible = countries.filter {
                        it.name.contains(search, ignoreCase = true) ||
                            it.dialCode.contains(search, ignoreCase = true)
                    }
                    if (visible.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No countries found", color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            items(visible, key = { it.id }) { item ->
                                val selected = item.id == countryId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(49.dp)
                                        .background(
                                            if (selected) Color(0xFFDCEBFC) else Color.White,
                                        )
                                        .clickable {
                                            countryId = item.id
                                            sheetOpen = false
                                            attempted = false
                                            previewMessage = false
                                        }
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PreviewFlag(item.id)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        item.name,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        color = Color(0xFF18263B),
                                        fontWeight =
                                            if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                    Text(
                                        item.dialCode,
                                        color = if (selected) brandBlue else Color(0xFF64748B),
                                        fontSize = 13.sp,
                                    )
                                    Spacer(Modifier.width(9.dp))
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(Color(0xFF1769D2), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("✓", color = Color.White, fontSize = 12.sp)
                                        }
                                    } else {
                                        Spacer(Modifier.size(18.dp))
                                    }
                                }
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = 16.dp)
                                        .background(Color(0xFFEDF1F5)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val brandBlue = Color(0xFF0A3A8D)

private data class PreviewCountry(val id: String, val name: String, val dialCode: String)

private val previewCountryRows = listOf(
    PreviewCountry("IN", "India", "+91"),
    PreviewCountry("US", "United States", "+1"),
    PreviewCountry("GB", "United Kingdom", "+44"),
    PreviewCountry("AE", "United Arab Emirates", "+971"),
    PreviewCountry("SA", "Saudi Arabia", "+966"),
    PreviewCountry("QA", "Qatar", "+974"),
    PreviewCountry("KW", "Kuwait", "+965"),
    PreviewCountry("OM", "Oman", "+968"),
)

/** Screen-reference-inspired abstract city shapes, not a licensed hero photograph. */
@Composable
private fun PreviewSkyline(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val base = size.height * 0.85f
        val widths = floatArrayOf(0.07f, 0.10f, 0.075f, 0.12f, 0.08f, 0.09f, 0.13f, 0.07f)
        var x = size.width * 0.02f
        widths.forEachIndexed { i, proportion ->
            val w = size.width * proportion
            val h = size.height * (0.23f + (i % 4) * 0.08f)
            drawRect(
                Color(0xFF7BB0CF).copy(alpha = 0.28f),
                topLeft = Offset(x, base - h),
                size = Size(w, h),
            )
            x += w + size.width * 0.027f
        }
    }
}

@Composable
private fun PreviewShield() {
    Canvas(Modifier.size(width = 26.dp, height = 30.dp)) {
        val shield = Path().apply {
            moveTo(size.width * 0.50f, 0f)
            lineTo(size.width, size.height * 0.18f)
            lineTo(size.width * 0.91f, size.height * 0.63f)
            lineTo(size.width * 0.50f, size.height)
            lineTo(size.width * 0.09f, size.height * 0.63f)
            lineTo(0f, size.height * 0.18f)
            close()
        }
        drawPath(shield, Brush.verticalGradient(listOf(Color(0xFF1787F7), Color(0xFF0A3A8D))))
        drawLine(
            Color.White,
            Offset(size.width * 0.25f, size.height * 0.49f),
            Offset(size.width * 0.44f, size.height * 0.67f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            Color.White,
            Offset(size.width * 0.44f, size.height * 0.67f),
            Offset(size.width * 0.76f, size.height * 0.31f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PreviewChevron() {
    Canvas(Modifier.size(12.dp)) {
        drawLine(
            brandBlue,
            Offset(size.width * 0.17f, size.height * 0.35f),
            Offset(size.width * 0.50f, size.height * 0.66f),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            brandBlue,
            Offset(size.width * 0.50f, size.height * 0.66f),
            Offset(size.width * 0.83f, size.height * 0.35f),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PreviewSearchIcon() {
    Canvas(Modifier.size(16.dp)) {
        drawCircle(
            Color(0xFF94A3B8),
            radius = size.minDimension * 0.29f,
            center = Offset(size.width * 0.42f, size.height * 0.40f),
            style = Stroke(1.5.dp.toPx()),
        )
        drawLine(
            Color(0xFF94A3B8),
            Offset(size.width * 0.63f, size.height * 0.64f),
            Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/** Lightweight preview-only geometric flag substitutes, never packaged product assets. */
@Composable
private fun PreviewFlag(id: String) {
    Canvas(
        Modifier
            .size(width = 24.dp, height = 18.dp)
            .clip(RoundedCornerShape(2.dp)),
    ) {
        val w = size.width
        val h = size.height
        fun stripe(color: Color, y: Float, height: Float) {
            drawRect(color, topLeft = Offset(0f, y), size = Size(w, height))
        }
        when (id) {
            "IN" -> {
                stripe(Color(0xFFFF9933), 0f, h / 3f)
                stripe(Color.White, h / 3f, h / 3f)
                stripe(Color(0xFF138808), h * 2f / 3f, h / 3f)
                drawCircle(Color(0xFF000080), radius = h * 0.11f)
            }
            "US" -> {
                stripe(Color.White, 0f, h)
                repeat(7) { i ->
                    stripe(Color(0xFFB22234), h * i * 2f / 13f, h / 13f)
                }
                drawRect(Color(0xFF3C3B6E), size = Size(w * 0.42f, h * 0.54f))
            }
            "GB" -> {
                stripe(Color(0xFF012169), 0f, h)
                drawRect(Color.White, topLeft = Offset(w * 0.39f, 0f), size = Size(w * 0.22f, h))
                drawRect(Color.White, topLeft = Offset(0f, h * 0.36f), size = Size(w, h * 0.28f))
                drawRect(Color(0xFFC8102E), topLeft = Offset(w * 0.44f, 0f), size = Size(w * 0.12f, h))
                drawRect(Color(0xFFC8102E), topLeft = Offset(0f, h * 0.42f), size = Size(w, h * 0.16f))
            }
            "AE" -> {
                stripe(Color(0xFF009739), 0f, h / 3f)
                stripe(Color.White, h / 3f, h / 3f)
                stripe(Color(0xFF000000), h * 2f / 3f, h / 3f)
                drawRect(Color(0xFFEF3340), size = Size(w * 0.26f, h))
            }
            "SA" -> {
                stripe(Color(0xFF006C35), 0f, h)
                drawLine(Color.White, Offset(w * 0.20f, h * 0.65f), Offset(w * 0.80f, h * 0.65f), 1.5.dp.toPx())
            }
            "QA" -> {
                stripe(Color(0xFF8A1538), 0f, h)
                drawRect(Color.White, size = Size(w * 0.33f, h))
            }
            "KW" -> {
                stripe(Color(0xFF007A3D), 0f, h / 3f)
                stripe(Color.White, h / 3f, h / 3f)
                stripe(Color(0xFFCE1126), h * 2f / 3f, h / 3f)
                drawRect(Color.Black, size = Size(w * 0.24f, h))
            }
            "OM" -> {
                stripe(Color.White, 0f, h / 3f)
                stripe(Color(0xFFDC143C), h / 3f, h / 3f)
                stripe(Color(0xFF008000), h * 2f / 3f, h / 3f)
                drawRect(Color(0xFFDC143C), size = Size(w * 0.24f, h))
            }
        }
    }
}
