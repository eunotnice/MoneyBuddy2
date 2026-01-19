package com.example.moneybuddy2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.moneybuddy2.ui.theme.MoneyBuddy2Theme
import androidx.navigation.compose.rememberNavController
import com.example.moneybuddy2.ui.navigation.NavGraph
import com.example.moneybuddy2.ui.navigation.Routes
import com.example.moneybuddy2.ui.theme.MoneyBuddy2Theme
import com.google.firebase.auth.FirebaseAuth
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyBuddy2Theme {
                val navController = rememberNavController()

                val start = if (FirebaseAuth.getInstance().currentUser != null) {
                    Routes.HOME
                } else {
                    Routes.LOGIN
                }

                NavGraph(
                    navController = navController,
                    startDestination = start
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoneyBuddy2Theme {
        Greeting("Android")
    }
}