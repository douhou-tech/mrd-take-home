package com.mrd.restaurant.discovery.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrd.restaurant.discovery.ui.R
import com.mrd.restaurant.discovery.ui.theme.Black70

@Composable
fun FavouriteIconButton(
    modifier: Modifier = Modifier,
    favourite: Boolean,
    onClick: () -> Unit
) {
    val icon = when {
        favourite -> Icons.Default.Favorite
        else -> Icons.Default.FavoriteBorder
    }
    val tint = when {
        favourite -> Color.Red
        else -> MaterialTheme.colorScheme.onSurface
    }
    FilledIconButton(
        modifier = modifier,
        onClick = onClick,
        colors = IconButtonColors(
            containerColor = Black70,
            contentColor = tint,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(32.dp),
        content = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.content_description_favourite),
            )
        }
    )
}
