package com.kutirakone.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kutirakone.app.model.enums.MaterialType

@Composable
fun MaterialBadge(material: MaterialType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(material.color, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = material.displayName,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
