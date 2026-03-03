package com.example.stugbygget.data.firebase.auth

import com.example.stugbygget.domain.repository.ProjectSessionRepository
import com.google.firebase.auth.FirebaseAuth

class FirebaseProjectSessionRepository(
    private val firebaseAuth: FirebaseAuth,
    private val configuredProjectId: String
) : ProjectSessionRepository {

    override fun getProjectId(): String {
        return configuredProjectId
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
