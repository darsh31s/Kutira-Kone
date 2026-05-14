package com.kutirakone.app.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kutirakone.app.model.enums.MaterialType

@Composable
fun MaterialFilterRow(
    selectedMaterial: MaterialType?,
    onMaterialSelected: (MaterialType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selectedMaterial == null,
                onClick = { onMaterialSelected(null) },
                label = { Text("All") }
            )
        }
        
        items(MaterialType.values()) { material ->
            FilterChip(
                selected = selectedMaterial == material,
                onClick = { onMaterialSelected(material) },
                label = { Text(material.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = material.color,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
