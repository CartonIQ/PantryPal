package com.cartoniq.pantrypal.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cartoniq.pantrypal.R
import com.cartoniq.pantrypal.data.PantryItemEntity
import com.cartoniq.pantrypal.ui.theme.Honey
import com.cartoniq.pantrypal.ui.theme.Sage
import com.cartoniq.pantrypal.ui.theme.Terracotta

@Composable
fun ExpiryRadar(
    eatToday: List<PantryItemEntity>,
    eatSoon: List<PantryItemEntity>,
    fresh: List<PantryItemEntity>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadarCard(R.string.radar_today, eatToday, Terracotta, true, Modifier.weight(1f).fillMaxHeight())
        RadarCard(R.string.radar_soon, eatSoon, Honey, false, Modifier.weight(1f).fillMaxHeight())
        RadarCard(R.string.radar_fresh, fresh, Sage, false, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun RadarCard(
    @StringRes label: Int,
    items: List<PantryItemEntity>,
    color: Color,
    pulsing: Boolean,
    modifier: Modifier
) {
    val pulse = rememberPulse(pulsing && items.isNotEmpty())
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = pulse))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Text(
                text = items.size.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            items.take(2).forEach {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (items.size > 2) {
                Text(
                    text = stringResource(R.string.radar_more, items.size - 2),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
