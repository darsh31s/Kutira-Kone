package com.kutirakone.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.model.Message
import com.kutirakone.app.ui.common.components.ChatMessageSkeleton
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    conversationId: String,
    otherPartyName: String,
    vendorId: String,
    listingId: String,
    chatViewModel: ChatViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user = remember { authViewModel.getCurrentUser() }
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val messageText by chatViewModel.messageText.collectAsState()
    val isCompleted by chatViewModel.isCompleted.collectAsState()
    val isArtisan = userProfile?.role == com.kutirakone.app.model.enums.UserRole.ARTISAN
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(conversationId, user) {
        user?.uid?.let { chatViewModel.loadMessages(conversationId, it) }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherPartyName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { chatViewModel.messageText.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    maxLines = 3
                )
                IconButton(onClick = {
                    user?.let { u ->
                        chatViewModel.sendMessage(conversationId, u.uid, userProfile?.name ?: "User")
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = KutiraGreen)
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            

            when (messages) {
                is UiState.Loading -> {
                    Column {
                        repeat(5) { ChatMessageSkeleton() }
                    }
                }
                is UiState.Success -> {
                    val msgs = (messages as UiState.Success).data
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        reverseLayout = false
                    ) {
                        items(msgs) { msg ->
                            val isMine = msg.senderId == user?.uid
                            MessageBubble(msg, isMine)
                        }
                    }
                }
                is UiState.Error -> Text((messages as UiState.Error).message)
                else -> {}
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(
                    if (isMine) KutiraGreen else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    color = if (isMine) Color.White else Color.Black
                )
                Text(
                    text = "", // Placeholder for timestamp formatting
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.DarkGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
