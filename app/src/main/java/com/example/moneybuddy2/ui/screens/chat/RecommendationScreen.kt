package com.example.moneybuddy2.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.data.model.RecommendationCard
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    vm: RecommendationViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadCurrentMonth()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recommendations") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            if (ui.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            ui.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val plan = ui.plan
                    if (plan != null) {
                        Card {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Budget Plan (${plan.ruleLabel})", style = MaterialTheme.typography.titleMedium)
                                Text("Income: ${plan.income}")
                                Text("Needs ≤ 50%: ${plan.needsLimit}")
                                Text("Wants ≤ 30%: ${plan.wantsLimit}")
                                Text("Savings/Debt ≥ 20%: ${plan.savingsLimit}")
                            }
                        }
                    }
                }

                items(ui.cards) { card ->
                    RecommendationCardView(
                        card = card,
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardView(
    card: RecommendationCard,
    onOpenUrl: (String) -> Unit
) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(card.title, style = MaterialTheme.typography.titleMedium)
            Text(card.message)

            if (card.sources.isNotEmpty()) {
                Divider()
                Text("Sources", style = MaterialTheme.typography.labelLarge)
                card.sources.forEach { s ->
                    TextButton(onClick = { onOpenUrl(s.url) }) {
                        Text(s.label)
                    }
                }
            }
        }
    }
}

