package com.example.moneybuddy2.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.data.model.ChatMessage
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import com.example.moneybuddy2.data.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: ChatViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyBuddy Chat") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            ui.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.messages) { message ->
                    MessageBubble(message)
                }
            }
            LaunchedEffect(ui.messages.size) {
                listState.animateScrollToItem(ui.messages.size)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text("Ask about spending or financial literacy…")
                    },
                    enabled = !ui.sending
                )

                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            vm.send(input)
                            input = ""
                        }
                    },
                    enabled = !ui.sending && input.isNotBlank()
                ) {
                    Text(if (ui.sending) "..." else "Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {

    val isUser = message.role == Role.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUser) Arrangement.End else Arrangement.Start
    ) {

        Box(
            modifier = Modifier
                .background(
                    color =
                        if (isUser)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {

            Text(
                text = message.text,
                color =
                    if (isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}