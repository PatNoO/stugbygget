package com.example.stugbygget

import android.app.Application
import com.example.stugbygget.di.AppContainer

class StugByggetApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
