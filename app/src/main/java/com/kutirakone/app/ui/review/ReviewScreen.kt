package com.kutirakone.app.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.common.components.StarRatingBar
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    vendorId: String,
    listingId: String,
    reviewViewModel: ReviewViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user = remember { authViewModel.getCurrentUser() }
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val selectedRating by reviewViewModel.selectedRating.collectAsState()
    val reviewComment by reviewViewModel.reviewComment.collectAsState()
    val submitState by reviewViewModel.submitState.collectAsState()

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            navController.navigate(Screen.CustomerDashboard.route) { popUpTo(0) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave a Review") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How was your experience?", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            StarRatingBar(
                rating = selectedRating.toDouble(),
                isInteractive = true,
                onRatingChanged = { reviewViewModel.setRating(it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = reviewComment,
                onValueChange = { if (it.length <= 300) reviewViewModel.reviewComment.value = it },
                label = { Text("Tell others about this trade...") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5
            )
            Text("${reviewComment.length}/300", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.End))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    user?.let {
                        reviewViewModel.submitReview(vendorId, it.uid, userProfile?.name ?: "User", listingId)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
            ) {
                if (submitState is UiState.Loading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text("Submit Review")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { navController.navigate(Screen.CustomerDashboard.route) { popUpTo(0) } }) {
                Text("Skip")
            }
        }
    }
}
