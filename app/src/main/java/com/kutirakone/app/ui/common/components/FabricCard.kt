package com.kutirakone.app.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.ListingType
import com.kutirakone.app.ui.theme.FreeColor
import com.kutirakone.app.ui.theme.SellColor
import com.kutirakone.app.ui.theme.SwapColor

@Composable
fun FabricCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDistanceAndRating: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (listing.safePhotoURLs.isNotEmpty()) {
                    AsyncImage(
                        model = listing.safePhotoURLs.first(),
                        contentDescription = "Fabric image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }

                MaterialBadge(
                    material = listing.material,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                val typeColor = when (listing.type) {
                    ListingType.SELL -> SellColor
                    ListingType.SWAP -> SwapColor
                    ListingType.FREE -> FreeColor
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(typeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = listing.type.name,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${listing.sizeMetres}m • ${listing.colour}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val priceText = when (listing.type) {
                    ListingType.SELL -> "₹${listing.price}"
                    ListingType.SWAP -> "SWAP"
                    ListingType.FREE -> "FREE"
                }
                
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (showDistanceAndRating) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (listing.distanceKm < 0.2) "Nearby" else "%.1f km away".format(listing.distanceKm),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "%.1f".format(listing.userAvgRating),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
