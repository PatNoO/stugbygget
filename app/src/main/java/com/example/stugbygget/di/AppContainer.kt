package com.example.stugbygget.di

import com.example.stugbygget.data.firebase.auth.FirebaseAuthRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhaseRepository
import com.example.stugbygget.domain.repository.AuthRepository
import com.example.stugbygget.domain.repository.PhaseRepository
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.SignInWithGoogleUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

class AppContainer {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(firebaseAuth) }
    val phaseRepository: PhaseRepository by lazy { FirestorePhaseRepository(firestore) }

    val observeAuthUserUseCase: ObserveAuthUserUseCase by lazy {
        ObserveAuthUserUseCase(authRepository)
    }
    val signInWithGoogleUseCase: SignInWithGoogleUseCase by lazy {
        SignInWithGoogleUseCase(authRepository)
    }
    val signOutUseCase: SignOutUseCase by lazy {
        SignOutUseCase(authRepository)
    }
    val observePhasesUseCase: ObservePhasesUseCase by lazy {
        ObservePhasesUseCase(phaseRepository)
    }
}
