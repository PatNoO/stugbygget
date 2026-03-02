package com.example.stugbygget.core.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineSyncCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val queue = ArrayDeque<suspend () -> Unit>()

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    fun onConnectivityChanged(isOnline: Boolean) {
        _state.update { it.copy(isOnline = isOnline) }
        if (isOnline) {
            scope.launch { flushPendingWrites() }
        }
    }

    suspend fun runOrQueue(action: suspend () -> Unit) {
        if (_state.value.isOnline) {
            runCatching { action() }.onFailure {
                enqueue(action, it.message)
            }
        } else {
            enqueue(action, "Device is offline. Write queued.")
        }
    }

    private suspend fun enqueue(action: suspend () -> Unit, error: String?) {
        mutex.withLock {
            queue.addLast(action)
            _state.update { it.copy(pendingWrites = queue.size, lastError = error) }
        }
    }

    private suspend fun flushPendingWrites() {
        if (!_state.value.isOnline) return
        _state.update { it.copy(isSyncing = true, lastError = null) }

        while (_state.value.isOnline) {
            val next = mutex.withLock {
                queue.removeFirstOrNull().also {
                    _state.update { state -> state.copy(pendingWrites = queue.size) }
                }
            } ?: break

            runCatching { next() }.onFailure { throwable ->
                enqueue(next, throwable.message ?: "Retry failed")
                _state.update { it.copy(isSyncing = false) }
                return
            }
        }
        _state.update { it.copy(isSyncing = false) }
    }
}
