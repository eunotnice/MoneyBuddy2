package com.example.moneybuddy2.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moneybuddy2.MoneyBuddyApp
import com.example.moneybuddy2.core.chat.BotAction
import com.example.moneybuddy2.core.chat.WhatsappHelper
import com.example.moneybuddy2.data.model.Role
import com.example.moneybuddy2.ui.navigation.Routes
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
                modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ui.messages) { m ->
                    val prefix = if (m.role == Role.USER) "You: " else "SBH: "
                    Text(prefix + m.text)
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


@Composable
fun ChatbotRoute(
    entry: NavBackStackEntry,
    app: MoneyBuddyApp,
    onBack: () -> Unit
) {
    // Use applicationContext to avoid capturing Activity
    val appContext = app.applicationContext

    // Log entry lifecycle for diagnosis
    LaunchedEffect(entry.id) {
        Log.d(TAG_CHATBOT, "ChatbotRoute entered: id=${entry.id}, route=${entry.destination.route}, state=${entry.lifecycle.currentState}")
    }

    val vm = remember(entry.id) {
        try {
            Log.d(TAG_CHATBOT, "Creating ChatbotViewModel (key=entry.id=${entry.id})")
            app.container.createCompanyBotViewModel(appContext)
        } catch (t: Throwable) {
            Log.e(TAG_CHATBOT, "Failed creating ChatbotViewModel", t)
            throw t
        }
    }

    DisposableEffect(entry.id) {
        onDispose {
            Log.d(TAG_CHATBOT, "ChatbotRoute disposed: id=${entry.id}")
        }
    }

    BotScreen(
        vm = vm,
        onBack = onBack,
        whatsappPhoneE164 = "601127275319"
    )
}