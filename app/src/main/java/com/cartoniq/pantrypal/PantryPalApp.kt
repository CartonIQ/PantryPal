package com.cartoniq.pantrypal

import android.app.Application
import android.content.Context
import com.cartoniq.pantrypal.viewmodel.KEY_REMINDERS
import com.cartoniq.pantrypal.viewmodel.PREFS_NAME
import com.cartoniq.pantrypal.viewmodel.Wallet
import com.cartoniq.pantrypal.work.ExpiryWorker

class PantryPalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Wallet.load(this)
        ExpiryWorker.createChannel(this)
        if (getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_REMINDERS, true)) {
            ExpiryWorker.schedule(this)
        }
    }
}
