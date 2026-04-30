package com.example.moneybuddy2.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.core.chat.BotAction
import com.example.moneybuddy2.core.chat.WhatsappHelper
import com.example.moneybuddy2.ui.viewmodel.ChatbotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotScreen (
    vm: ChatbotViewModel,
    onBack: () -> Unit,
    whatsappPhoneE164: String
){
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }

    // Store last action to decide showing WA button
    var lastAction by remember { mutableStateOf<BotAction?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(ui.messages.size) {
        listState.animateScrollToItem(ui.messages.size)
    }
    Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("SBH Chat") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.messages) { message ->
                    MessageBubble(message)
                }
            }

            // Quick replies
            if (ui.quickReplies.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.quickReplies) { label ->
                        AssistChip(
                            onClick = { lastAction = vm.send(label) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // WhatsApp CTA if last action is OpenWhatsApp
            if (lastAction is BotAction.OpenWhatsapp) {
                val msg = (lastAction as BotAction.OpenWhatsapp).prefillMessage
                Button(
                    onClick = { WhatsappHelper.open(context, whatsappPhoneE164, msg) },
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Text("Book via WhatsApp")
                }
            }

            // Input box
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask a question or type “book consultation”…") }
                )
                Button(onClick = {
                    lastAction = vm.send(input)
                    input = ""
                }) { Text("Send") }
            }
        }
    }
}

private const val TAG_CHATBOT = "ChatbotRoute"

//@Composable
//fun MessageBubble(message: ChatMessage){
//    val isUser = message.role == Role.USER
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = if(isUser) Arrangement.End else Arrangement.Start
//    ) {
//        if(!isUser){
//            Icon(
//                imageVector = Icons.Default.SmartToy,
//                contentDescription = null,
//                modifier = Modifier
//                    .size(32.dp)
//                    .padding(end = 6.dp)
//            )
//        }
//
//        Card(
//            colors = CardDefaults.cardColors(
//                containerColor =
//                    if(isUser)
//                        MaterialTheme.colorScheme.primary
//                    else
//                        MaterialTheme.colorScheme.surfaceVariant
//            ),
//            shape = RoundedCornerShape(16.dp),
//            modifier = Modifier.padding(4.dp)
//        ){
//            Text(
//                text = message.text,
//                modifier = Modifier.padding(12.dp),
//                color = if (isUser) Color.White else Color.Black
//            )
//        }
//
//        if(isUser){
//            Icon(
//                imageVector = Icons.Default.Person,
//                contentDescription = null,
//                modifier = Modifier
//                    .size(32.dp)
//                    .padding(start = 6.dp)
//            )
//        }
//
//    }
//}



