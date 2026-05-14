package com.kutirakone.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kutirakone.app.model.DesignIdea
import com.kutirakone.app.ui.theme.EasyColor
import com.kutirakone.app.ui.theme.HardColor
import com.kutirakone.app.ui.theme.MediumColor

@Composable
fun AIIdeaCard(idea: DesignIdea, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = idea.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                val diffColor = when(idea.difficulty.lowercase()) {
                    "easy" -> EasyColor
                    "medium" -> MediumColor
                    "hard" -> HardColor
                    else -> Color.Gray
                }
                
                Box(
                    modifier = Modifier
                        .background(diffColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = idea.difficulty,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = idea.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
