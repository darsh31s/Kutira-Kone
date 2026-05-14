package com.kutirakone.app.ui.inspire

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.ui.common.components.AIIdeaCard
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraAmber
import com.kutirakone.app.viewmodel.AIViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspireScreen(
    navController: NavController,
    aiViewModel: AIViewModel = viewModel()
) {
    val inspireMaterial by aiViewModel.inspireMaterial.collectAsState()
    val inspireSize by aiViewModel.inspireSize.collectAsState()
    val ideasState by aiViewModel.designIdeas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✨ Inspire Me") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = inspireMaterial,
                onValueChange = { aiViewModel.inspireMaterial.value = it },
                label = { Text("Material (e.g. Silk, Cotton)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inspireSize,
                onValueChange = { aiViewModel.inspireSize.value = it },
                label = { Text("Size in metres (e.g. 0.5)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { aiViewModel.generateInspireIdeas() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KutiraAmber)
            ) {
                Text("Generate Ideas", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (ideasState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val ideas = (ideasState as UiState.Success).data
                    Text("AI-generated ideas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ideas) { idea ->
                            AIIdeaCard(idea)
                        }
                    }
                }
                is UiState.Error -> {
                    Text((ideasState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}
