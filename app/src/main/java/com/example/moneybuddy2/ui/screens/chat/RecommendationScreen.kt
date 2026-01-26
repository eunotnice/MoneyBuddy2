package com.example.moneybuddy2.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.core.recommendation.Recommendation
import com.example.moneybuddy2.core.recommendation.RecommendationType
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    vm: RecommendationViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recommendations") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->

        when {
            ui.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            ui.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = ui.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                RecommendationContent(
                    modifier = Modifier.padding(padding),
                    aiExplanation = ui.aiExplanation,
                    recommendations = ui.recommendations
                )
            }
        }
    }
}

@Composable
private fun RecommendationContent(
    modifier: Modifier = Modifier,
    aiExplanation: String?,
    recommendations: List<Recommendation>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- AI explanation (main value) ---
        if (!aiExplanation.isNullOrBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = aiExplanation,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // --- System recommendations (optional, transparent) ---
        if (recommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Why these suggestions?",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(recommendations) { rec ->
                RecommendationCard(rec)
            }
        }
    }
}

@Composable
private fun RecommendationCard(rec: Recommendation) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (rec.type) {
                    RecommendationType.REDUCE_CATEGORY ->
                        "Reduce spending in ${rec.category}"
                    RecommendationType.INCREASE_SAVING ->
                        "Increase monthly savings"
                    RecommendationType.SET_BUDGET ->
                        "Set a budget"
                    RecommendationType.PAUSE_SPENDING ->
                        "Pause spending"

                    RecommendationType.REVIEW_SUBSCRIPTIONS -> TODO()
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = rec.rationale,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Potential impact: RM ${"%.2f".format(rec.monthlyImpact)} / month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


