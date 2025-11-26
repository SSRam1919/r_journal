// app/src/main/java/com/baverika/r_journal/RJournalApp.kt
package com.baverika.r_journal

import android.app.Application
import com.baverika.r_journal.data.remote.RetrofitClient
import com.baverika.r_journal.data.remote.ServerPrefs

class RJournalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val hostPort = ServerPrefs.getHostPort(this)
        RetrofitClient.setHostPort(hostPort)
    }
}
