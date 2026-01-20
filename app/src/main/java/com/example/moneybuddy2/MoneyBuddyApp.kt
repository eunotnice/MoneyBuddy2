package com.example.moneybuddy2

import android.app.Application
import com.example.moneybuddy2.di.AppContainer

class MoneyBuddyApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
