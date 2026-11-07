// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.domain.billing.PurchaseStatus
import com.gerstudio.cleanbot.domain.billing.TipOption

/** External links for supporting and connecting with the developer */
const val URL_BUY_ME_A_COFFEE = "https://buymeacoffee.com/mayankmeena"
const val URL_PLAY_STORE_DEV = "https://play.google.com/store/apps/dev?id=8895908145914606687"
const val URL_OFFICIAL_WEBSITE = "https://www.gerstudio.com/"
const val URL_GITHUB_REPO = "https://github.com/dcryptoniun/cleanbot"
const val URL_GITHUB_SPONSORS = "https://github.com/sponsors/mayankmeena"

@Composable
fun SupportScreen(
    tipOptions: List<TipOption>,
    purchaseStatus: PurchaseStatus,
    onSendTip: (Activity, TipOption) -> Unit,
    onDismissStatus: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "Support cLeanBot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Hero Card
        HeroSupportCard()

        // Section: In-App Tips (Google Play Billing)
        Text(
            text = "Tip Jar (In-App Purchases)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "All tips are consumable and can be sent multiple times. Thank you for fueling open-source development!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tips Grid/List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tipOptions.forEach { tip ->
                TipOptionCard(
                    tip = tip,
                    isProcessing = purchaseStatus is PurchaseStatus.Processing && purchaseStatus.productId == tip.productId,
                    onBuy = {
                        if (activity != null) {
                            onSendTip(activity, tip)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section: External Support & Other Apps
        Text(
            text = "External Support & Community",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        ExternalLinkCard(
            title = "Buy Me a Coffee",
            subtitle = "Support via buymeacoffee.com/mayankmeena",
            icon = Icons.Default.Coffee,
            tint = Color(0xFFFFDD00),
            onClick = { openUrl(context, URL_BUY_ME_A_COFFEE) }
        )

        ExternalLinkCard(
            title = "Explore Our Other Apps",
            subtitle = "Discover more tools by Ger Studio on Google Play",
            icon = Icons.Default.Shop,
            tint = MaterialTheme.colorScheme.primary,
            onClick = { openUrl(context, URL_PLAY_STORE_DEV) }
        )

        ExternalLinkCard(
            title = "GitHub Sponsors",
            subtitle = "Become a sponsor at github.com/sponsors/mayankmeena",
            icon = Icons.Default.VolunteerActivism,
            tint = Color(0xFFEA4AAA),
            onClick = { openUrl(context, URL_GITHUB_SPONSORS) }
        )

        ExternalLinkCard(
            title = "GitHub Repository",
            subtitle = "Star, view source, or contribute: github.com/dcryptoniun/cleanbot",
            icon = Icons.Default.Favorite,
            tint = MaterialTheme.colorScheme.secondary,
            onClick = { openUrl(context, URL_GITHUB_REPO) }
        )

        ExternalLinkCard(
            title = "Official Website",
            subtitle = "Visit www.gerstudio.com",
            icon = Icons.Default.Language,
            tint = MaterialTheme.colorScheme.tertiary,
            onClick = { openUrl(context, URL_OFFICIAL_WEBSITE) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Status / Thank You Dialog
    when (purchaseStatus) {
        is PurchaseStatus.Success -> {
            AlertDialog(
                onDismissRequest = onDismissStatus,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        "Heartfelt Thanks!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        purchaseStatus.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismissStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("You're Welcome!")
                    }
                }
            )
        }
        is PurchaseStatus.Error -> {
            AlertDialog(
                onDismissRequest = onDismissStatus,
                title = { Text("Notice") },
                text = { Text(purchaseStatus.message) },
                confirmButton = {
                    TextButton(onClick = onDismissStatus) {
                        Text("OK")
                    }
                }
            )
        }
        else -> Unit
    }
}

@Composable
private fun HeroSupportCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Free & Open Source",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero ads • Zero trackers • Pure living room bliss",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "cLeanBot is built with love to keep Android TV fast, clean, and private. If cLeanBot saved your TV from bloatware or made your home setup smoother, you can support ongoing development with a tip or by checking out our other projects!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TipOptionCard(
    tip: TipOption,
    isProcessing: Boolean,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = tip.emoji, fontSize = 22.sp)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onBuy,
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = tip.formattedPrice ?: tip.defaultPrice,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
