package com.example.stugbygget.di

import com.example.stugbygget.data.firebase.auth.FirebaseAuthRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhaseRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhotoRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreTodoRepository
import com.example.stugbygget.domain.repository.AuthRepository
import com.example.stugbygget.domain.repository.PhaseRepository
import com.example.stugbygget.domain.repository.PhotoRepository
import com.example.stugbygget.domain.repository.TodoRepository
import com.example.stugbygget.domain.usecase.DeletePhotoUseCase
import com.example.stugbygget.domain.usecase.DeleteTodoUseCase
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.SignInWithGoogleUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import com.example.stugbygget.domain.usecase.UploadPhotoUseCase
import com.example.stugbygget.domain.usecase.UpsertTodoUseCase
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
    val todoRepository: TodoRepository by lazy { FirestoreTodoRepository(firestore) }
    val photoRepository: PhotoRepository by lazy { FirestorePhotoRepository(firestore, storage) }

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
    val observeTodosUseCase: ObserveTodosUseCase by lazy {
        ObserveTodosUseCase(todoRepository)
    }
    val upsertTodoUseCase: UpsertTodoUseCase by lazy {
        UpsertTodoUseCase(todoRepository)
    }
    val toggleTodoUseCase: ToggleTodoUseCase by lazy {
        ToggleTodoUseCase(todoRepository)
    }
    val deleteTodoUseCase: DeleteTodoUseCase by lazy {
        DeleteTodoUseCase(todoRepository)
    }
    val observePhotosUseCase: ObservePhotosUseCase by lazy {
        ObservePhotosUseCase(photoRepository)
    }
    val uploadPhotoUseCase: UploadPhotoUseCase by lazy {
        UploadPhotoUseCase(photoRepository)
    }
    val deletePhotoUseCase: DeletePhotoUseCase by lazy {
        DeletePhotoUseCase(photoRepository)
    }
}
