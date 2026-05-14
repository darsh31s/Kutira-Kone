package com.kutirakone.app.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RadiusFilterChips(
    selectedRadius: Double,
    onRadiusSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(1.0, 2.0, 5.0, 10.0, 20.0)
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(options) { radius ->
            val isSelected = selectedRadius == radius
            FilterChip(
                selected = isSelected,
                onClick = { onRadiusSelected(radius) },
                label = { Text("${radius.toInt()}km") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
