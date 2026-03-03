package com.example.stugbygget.data.ar

import android.content.Context
import com.example.stugbygget.domain.repository.ArSessionRepository
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session

class ArCoreSessionRepository(
    private val appContext: Context
) : ArSessionRepository {
    private var session: Session? = null

    override fun isSupported(): Boolean {
        return ArCoreApk.getInstance().checkAvailability(appContext).isSupported
    }

    override fun startSession(): Result<Unit> {
        return runCatching {
            session?.close()
            session = Session(appContext)
        }.map {}
    }

    override fun stopSession() {
        session?.close()
        session = null
    }
}
