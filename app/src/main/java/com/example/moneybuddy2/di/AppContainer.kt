package com.example.moneybuddy2.di

import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.MoneyRepositoryImpl

class AppContainer {
    val repository: MoneyRepository = MoneyRepositoryImpl()
}