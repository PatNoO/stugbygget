package com.example.stugbygget.di

import android.content.Context
import com.example.stugbygget.BuildConfig
import com.example.stugbygget.core.offline.ConnectivityMonitor
import com.example.stugbygget.core.offline.OfflineSyncCoordinator
import com.example.stugbygget.data.firebase.auth.FirebaseAuthRepository
import com.example.stugbygget.data.firebase.auth.FirebaseProjectSessionRepository
import com.example.stugbygget.data.firebase.config.FirebaseRuntimeConfigRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhaseRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhotoRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePriceRecommendationRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreProjectContextProvider
import com.example.stugbygget.data.firebase.firestore.FirestoreShoppingRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreTodoRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreBudgetRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreContactRepository
import com.example.stugbygget.data.local.LocalNotificationSettingsRepository
import com.example.stugbygget.data.local.AndroidNotificationDispatcher
import com.example.stugbygget.data.remote.claude.ClaudeChatRepository
import com.example.stugbygget.domain.repository.AuthRepository
import com.example.stugbygget.domain.repository.ChatRepository
import com.example.stugbygget.domain.repository.ShoppingRepository
import com.example.stugbygget.domain.repository.BudgetRepository
import com.example.stugbygget.domain.repository.PhaseRepository
import com.example.stugbygget.domain.repository.PhotoRepository
import com.example.stugbygget.domain.repository.ProjectSessionRepository
import com.example.stugbygget.domain.repository.PriceRecommendationRepository
import com.example.stugbygget.domain.repository.ContactRepository
import com.example.stugbygget.domain.repository.NotificationDispatchGateway
import com.example.stugbygget.domain.repository.NotificationSettingsRepository
import com.example.stugbygget.domain.repository.RuntimeConfigRepository
import com.example.stugbygget.domain.repository.MaterialRepository
import com.example.stugbygget.domain.repository.OwnedMaterialRepository
import com.example.stugbygget.domain.repository.TodoRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreMaterialRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreOwnedMaterialRepository
import com.example.stugbygget.domain.usecase.DeleteContactUseCase
import com.example.stugbygget.domain.usecase.DeleteOwnedMaterialUseCase
import com.example.stugbygget.domain.usecase.DeletePhaseUseCase
import com.example.stugbygget.domain.usecase.DeletePhotoUseCase
import com.example.stugbygget.domain.usecase.DeleteTodoUseCase
import com.example.stugbygget.domain.usecase.CalculateMaterialQuantityUseCase
import com.example.stugbygget.domain.usecase.CompareShoppingPricesUseCase
import com.example.stugbygget.domain.usecase.AddShoppingItemUseCase
import com.example.stugbygget.domain.usecase.CreateShoppingListUseCase
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.ObserveContactsUseCase
import com.example.stugbygget.domain.usecase.UpsertContactUseCase
import com.example.stugbygget.domain.usecase.GetNotificationSettingsUseCase
import com.example.stugbygget.domain.usecase.GetRuntimeConfigUseCase
import com.example.stugbygget.domain.usecase.ObserveMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObserveOwnedMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import com.example.stugbygget.domain.usecase.ObservePriceQuotesUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.ObserveShoppingListsUseCase
import com.example.stugbygget.domain.usecase.ObserveBudgetOverviewUseCase
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.SignInWithEmailPasswordUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import com.example.stugbygget.domain.usecase.StreamAssistantReplyUseCase
import com.example.stugbygget.domain.usecase.RunNotificationPipelineUseCase
import com.example.stugbygget.domain.usecase.BuildNotificationEventsUseCase
import com.example.stugbygget.domain.usecase.BuildPlanningOverviewUseCase
import com.example.stugbygget.domain.usecase.DispatchNotificationEventsUseCase
import com.example.stugbygget.domain.usecase.FetchRuntimeConfigUseCase
import com.example.stugbygget.domain.usecase.UpdateNotificationSettingsUseCase
import com.example.stugbygget.domain.usecase.ToggleShoppingItemPurchasedUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import com.example.stugbygget.domain.usecase.UploadPhotoUseCase
import com.example.stugbygget.domain.usecase.SeedMaterialsUseCase
import com.example.stugbygget.domain.usecase.UpsertOwnedMaterialUseCase
import com.example.stugbygget.domain.usecase.UpsertPhaseUseCase
import com.example.stugbygget.domain.usecase.UpsertTodoUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage

class AppContainer(
    appContext: Context
) {
    val applicationContext: Context = appContext.applicationContext

    val offlineSyncCoordinator: OfflineSyncCoordinator by lazy { OfflineSyncCoordinator() }
    private val connectivityMonitor: ConnectivityMonitor by lazy {
        ConnectivityMonitor(applicationContext) { online ->
            offlineSyncCoordinator.onConnectivityChanged(online)
        }
    }

    init {
        connectivityMonitor.start()
    }

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().also { db ->
            db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }
    val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(firebaseAuth) }
    val projectSessionRepository: ProjectSessionRepository by lazy {
        FirebaseProjectSessionRepository(
            firebaseAuth = firebaseAuth,
            configuredProjectId = BuildConfig.PROJECT_ID
        )
    }
    val phaseRepository: PhaseRepository by lazy { FirestorePhaseRepository(firestore) }
    val todoRepository: TodoRepository by lazy { FirestoreTodoRepository(firestore, offlineSyncCoordinator) }
    val shoppingRepository: ShoppingRepository by lazy {
        FirestoreShoppingRepository(firestore, offlineSyncCoordinator)
    }
    val budgetRepository: BudgetRepository by lazy { FirestoreBudgetRepository(firestore) }
    val contactRepository: ContactRepository by lazy { FirestoreContactRepository(firestore) }
    val photoRepository: PhotoRepository by lazy { FirestorePhotoRepository(firestore, storage) }
    val materialRepository: MaterialRepository by lazy {
        FirestoreMaterialRepository(firestore)
    }
    val ownedMaterialRepository: OwnedMaterialRepository by lazy {
        FirestoreOwnedMaterialRepository(firestore)
    }
    val priceRecommendationRepository: PriceRecommendationRepository by lazy {
        FirestorePriceRecommendationRepository(firestore)
    }
    val projectContextProvider: FirestoreProjectContextProvider by lazy {
        FirestoreProjectContextProvider(firestore)
    }
    val runtimeConfigRepository: RuntimeConfigRepository by lazy {
        FirebaseRuntimeConfigRepository(remoteConfig)
    }
    val chatRepository: ChatRepository by lazy {
        ClaudeChatRepository(
            functions = functions,
            contextProvider = projectContextProvider
        )
    }
    val notificationSettingsRepository: NotificationSettingsRepository by lazy {
        LocalNotificationSettingsRepository(applicationContext)
    }
    val notificationDispatchGateway: NotificationDispatchGateway by lazy {
        AndroidNotificationDispatcher(applicationContext)
    }
    val observeAuthUserUseCase: ObserveAuthUserUseCase by lazy {
        ObserveAuthUserUseCase(authRepository)
    }
    val signInWithEmailPasswordUseCase: SignInWithEmailPasswordUseCase by lazy {
        SignInWithEmailPasswordUseCase(authRepository)
    }
    val signOutUseCase: SignOutUseCase by lazy {
        SignOutUseCase(authRepository)
    }
    val observePhasesUseCase: ObservePhasesUseCase by lazy {
        ObservePhasesUseCase(phaseRepository)
    }
    val buildPlanningOverviewUseCase: BuildPlanningOverviewUseCase by lazy {
        BuildPlanningOverviewUseCase()
    }
    val observeTodosUseCase: ObserveTodosUseCase by lazy {
        ObserveTodosUseCase(todoRepository)
    }
    val observeShoppingListsUseCase: ObserveShoppingListsUseCase by lazy {
        ObserveShoppingListsUseCase(shoppingRepository)
    }
    val observeBudgetOverviewUseCase: ObserveBudgetOverviewUseCase by lazy {
        ObserveBudgetOverviewUseCase(budgetRepository)
    }
    val createShoppingListUseCase: CreateShoppingListUseCase by lazy {
        CreateShoppingListUseCase(shoppingRepository)
    }
    val addShoppingItemUseCase: AddShoppingItemUseCase by lazy {
        AddShoppingItemUseCase(shoppingRepository)
    }
    val toggleShoppingItemPurchasedUseCase: ToggleShoppingItemPurchasedUseCase by lazy {
        ToggleShoppingItemPurchasedUseCase(shoppingRepository)
    }
    val compareShoppingPricesUseCase: CompareShoppingPricesUseCase by lazy {
        CompareShoppingPricesUseCase(priceRecommendationRepository)
    }
    val getNotificationSettingsUseCase: GetNotificationSettingsUseCase by lazy {
        GetNotificationSettingsUseCase(notificationSettingsRepository)
    }
    val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase by lazy {
        UpdateNotificationSettingsUseCase(notificationSettingsRepository)
    }
    val buildNotificationEventsUseCase: BuildNotificationEventsUseCase by lazy {
        BuildNotificationEventsUseCase()
    }
    val dispatchNotificationEventsUseCase: DispatchNotificationEventsUseCase by lazy {
        DispatchNotificationEventsUseCase(notificationDispatchGateway)
    }
    val runNotificationPipelineUseCase: RunNotificationPipelineUseCase by lazy {
        RunNotificationPipelineUseCase(
            getNotificationSettingsUseCase = getNotificationSettingsUseCase,
            buildNotificationEventsUseCase = buildNotificationEventsUseCase,
            dispatchNotificationEventsUseCase = dispatchNotificationEventsUseCase
        )
    }
    val upsertTodoUseCase: UpsertTodoUseCase by lazy {
        UpsertTodoUseCase(todoRepository)
    }
    val upsertPhaseUseCase: UpsertPhaseUseCase by lazy {
        UpsertPhaseUseCase(phaseRepository)
    }
    val deletePhaseUseCase: DeletePhaseUseCase by lazy {
        DeletePhaseUseCase(phaseRepository)
    }
    val toggleTodoUseCase: ToggleTodoUseCase by lazy {
        ToggleTodoUseCase(todoRepository)
    }
    val deleteTodoUseCase: DeleteTodoUseCase by lazy {
        DeleteTodoUseCase(todoRepository)
    }
    val observeMaterialsUseCase: ObserveMaterialsUseCase by lazy {
        ObserveMaterialsUseCase(materialRepository)
    }
    val seedMaterialsUseCase: SeedMaterialsUseCase by lazy {
        SeedMaterialsUseCase(materialRepository)
    }
    val observeOwnedMaterialsUseCase: ObserveOwnedMaterialsUseCase by lazy {
        ObserveOwnedMaterialsUseCase(ownedMaterialRepository)
    }
    val upsertOwnedMaterialUseCase: UpsertOwnedMaterialUseCase by lazy {
        UpsertOwnedMaterialUseCase(ownedMaterialRepository)
    }
    val deleteOwnedMaterialUseCase: DeleteOwnedMaterialUseCase by lazy {
        DeleteOwnedMaterialUseCase(ownedMaterialRepository)
    }
    val observePriceQuotesUseCase: ObservePriceQuotesUseCase by lazy {
        ObservePriceQuotesUseCase(materialRepository)
    }
    val calculateMaterialQuantityUseCase: CalculateMaterialQuantityUseCase by lazy {
        CalculateMaterialQuantityUseCase()
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
    val streamAssistantReplyUseCase: StreamAssistantReplyUseCase by lazy {
        StreamAssistantReplyUseCase(chatRepository)
    }
    val fetchRuntimeConfigUseCase: FetchRuntimeConfigUseCase by lazy {
        FetchRuntimeConfigUseCase(runtimeConfigRepository)
    }
    val getRuntimeConfigUseCase: GetRuntimeConfigUseCase by lazy {
        GetRuntimeConfigUseCase(runtimeConfigRepository)
    }
    val observeContactsUseCase: ObserveContactsUseCase by lazy {
        ObserveContactsUseCase(contactRepository)
    }
    val upsertContactUseCase: UpsertContactUseCase by lazy {
        UpsertContactUseCase(contactRepository)
    }
    val deleteContactUseCase: DeleteContactUseCase by lazy {
        DeleteContactUseCase(contactRepository)
    }
}
