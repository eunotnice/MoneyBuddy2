package com.example.moneybuddy2.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.data.model.ChatMessage
import com.example.moneybuddy2.data.model.Role
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─── Colour tokens (kept consistent with AiRecommendationScreen) ───────────
private val GreenMint     = Color(0xFF00C896)
private val GreenDark     = Color(0xFF009E78)
private val GreenBubble   = Color(0xFFE6FBF5)
private val SurfaceGray   = Color(0xFFF7F8FA)
private val BotBubble     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val RedSoft       = Color(0xFFFF6B6B)
private val RedLight      = Color(0xFFFFECEC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    vm: ChatViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(ui.messages.size) {
        if (ui.messages.isNotEmpty()) {
            listState.animateScrollToItem(ui.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bot avatar in top bar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(GreenMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                "MoneyBuddy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Your financial assistant",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                input = input,
                onInputChange = { input = it },
                sending = ui.sending,
                onSend = {
                    if (input.isNotBlank()) {
                        vm.send(input)
                        input = ""
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
            // Error banner
            AnimatedVisibility(visible = ui.error != null) {
                ui.error?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedLight)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", color = RedSoft, fontSize = 14.sp)
                        Text(err, color = RedSoft, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Empty state
            if (ui.messages.isEmpty()) {
                EmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.messages) { message ->
                        MessageBubble(message)
                    }

                    // Typing indicator
                    if (ui.sending) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GreenBubble),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.AccountBalance,
                contentDescription = null,
                tint = GreenMint,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Hi! I'm MoneyBuddy 👋",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Ask me anything about your spending,\nbudgeting, or financial goals.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))

        // Suggestion chips
        val suggestions = listOf(
            "How am I doing this month?",
            "Tips to save more",
            "What's my biggest expense?"
        )
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { /* vm.send(suggestion) */ },
                label = { Text(suggestion, fontSize = 13.sp) },
                modifier = Modifier.padding(vertical = 4.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color.White,
                    labelColor = GreenDark
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = GreenMint
                )
            )
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == Role.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender label
        Text(
            text = if (isUser) "You" else "MoneyBuddy",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Bot avatar dot
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp, bottom = 2.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GreenMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .background(if (isUser) GreenMint else BotBubble)
                    .then(
                        if (!isUser) Modifier.padding(1.dp) else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isUser) Color.White else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }

            // User avatar dot
            if (isUser) {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp, bottom = 2.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(end = 6.dp, bottom = 2.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(GreenMint),
            contentAlignment = Alignment.Center
        ) {
            Text("M", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                .background(BotBubble)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, delayMillis = index * 120, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = offsetY.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GreenMint)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    sending: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Ask about spending or budgeting…",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                enabled = !sending,
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GreenMint,
                    unfocusedBorderColor = DividerColor,
                    focusedContainerColor   = SurfaceGray,
                    unfocusedContainerColor = SurfaceGray
                )
            )

            // Send button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (input.isNotBlank() && !sending) GreenMint
                        else Color(0xFFE5E7EB)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GreenMint,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onSend,
                        enabled = input.isNotBlank() && !sending
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (input.isNotBlank()) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}