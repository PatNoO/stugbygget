package com.example.stugbygget.core.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class ConnectivityMonitor(
    context: Context,
    private val onConnectivityChanged: (Boolean) -> Unit
) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private var callbackRegistered = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onConnectivityChanged(true)
        }

        override fun onLost(network: Network) {
            onConnectivityChanged(hasValidatedNetwork())
        }

        override fun onUnavailable() {
            onConnectivityChanged(false)
        }
    }

    fun start() {
        if (callbackRegistered) return
        callbackRegistered = true
        onConnectivityChanged(hasValidatedNetwork())
        manager.registerDefaultNetworkCallback(callback)
    }

    private fun hasValidatedNetwork(): Boolean {
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
