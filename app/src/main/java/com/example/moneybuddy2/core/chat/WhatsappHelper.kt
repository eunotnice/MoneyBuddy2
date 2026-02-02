package com.example.moneybuddy2.core.chat

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsappHelper {

    fun open(context: Context, phoneE164: String, message: String) {
        val url = "https://wa.me/${phoneE164}?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}