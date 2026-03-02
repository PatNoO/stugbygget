package com.example.stugbygget

import android.app.Application
import com.example.stugbygget.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StugByggetApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            container.fetchRuntimeConfigUseCase()
        }
    }
}
