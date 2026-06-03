package com.cinemaverse.mcu.util

import android.content.Context
import android.content.Intent
import com.cinemaverse.mcu.activities.MainActivity

object AppRestarter {
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}
