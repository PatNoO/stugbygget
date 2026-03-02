package com.example.stugbygget.di

import android.content.Context
import com.example.stugbygget.BuildConfig
import com.example.stugbygget.data.firebase.auth.FirebaseAuthRepository
import com.example.stugbygget.data.firebase.config.FirebaseRuntimeConfigRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhaseRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreMeasurementRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePhotoRepository
import com.example.stugbygget.data.firebase.firestore.FirestorePriceRecommendationRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreProjectContextProvider
import com.example.stugbygget.data.firebase.firestore.FirestoreShoppingRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreTodoRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreBudgetRepository
import com.example.stugbygget.data.firebase.firestore.FirestoreLogisticsRepository
import com.example.stugbygget.data.local.LocalRoomDimensionsRepository
import com.example.stugbygget.data.local.LocalRoomLayoutDataSource
import com.example.stugbygget.data.local.LocalRoomLayoutRepository
import com.example.stugbygget.data.local.LocalNotificationSettingsRepository
import com.example.stugbygget.data.local.RouteCacheDataSource
import com.example.stugbygget.data.local.AndroidNotificationDispatcher
import com.example.stugbygget.data.remote.claude.ClaudeApiService
import com.example.stugbygget.data.remote.claude.ClaudeChatRepository
import com.example.stugbygget.data.remote.maps.GoogleDirectionsService
import com.example.stugbygget.data.remote.maps.GoogleRouteRepository
import com.example.stugbygget.domain.repository.AuthRepository
import com.example.stugbygget.domain.repository.ChatRepository
import com.example.stugbygget.domain.repository.ShoppingRepository
import com.example.stugbygget.domain.repository.BudgetRepository
import com.example.stugbygget.domain.repository.PhaseRepository
import com.example.stugbygget.domain.repository.PhotoRepository
import com.example.stugbygget.domain.repository.MeasurementRepository
import com.example.stugbygget.domain.repository.PriceRecommendationRepository
import com.example.stugbygget.domain.repository.LogisticsRepository
import com.example.stugbygget.domain.repository.NotificationDispatchGateway
import com.example.stugbygget.domain.repository.NotificationSettingsRepository
import com.example.stugbygget.domain.repository.RoomDimensionsRepository
import com.example.stugbygget.domain.repository.RoomLayoutRepository
import com.example.stugbygget.domain.repository.RouteRepository
import com.example.stugbygget.domain.repository.RuntimeConfigRepository
import com.example.stugbygget.domain.repository.TodoRepository
import com.example.stugbygget.domain.usecase.DeletePhotoUseCase
import com.example.stugbygget.domain.usecase.DeleteTodoUseCase
import com.example.stugbygget.domain.usecase.CalculateMeasurementDistanceUseCase
import com.example.stugbygget.domain.usecase.CompareShoppingPricesUseCase
import com.example.stugbygget.domain.usecase.AddShoppingItemUseCase
import com.example.stugbygget.domain.usecase.CreateShoppingListUseCase
import com.example.stugbygget.domain.usecase.CalculateLogisticsRecommendationUseCase
import com.example.stugbygget.domain.usecase.ExportMeasurementToRoomPlannerUseCase
import com.example.stugbygget.domain.usecase.GetRoomDimensionsUseCase
import com.example.stugbygget.domain.usecase.MeasurementUnitConverter
import com.example.stugbygget.domain.usecase.MoveFurnitureUseCase
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.GetRouteMetricsUseCase
import com.example.stugbygget.domain.usecase.GetNotificationSettingsUseCase
import com.example.stugbygget.domain.usecase.GetRuntimeConfigUseCase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.ObserveRoomLayoutUseCase
import com.example.stugbygget.domain.usecase.ObserveShoppingListsUseCase
import com.example.stugbygget.domain.usecase.ObserveBudgetOverviewUseCase
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.SaveMeasurementUseCase
import com.example.stugbygget.domain.usecase.SaveRoomLayoutUseCase
import com.example.stugbygget.domain.usecase.SignInWithGoogleUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import com.example.stugbygget.domain.usecase.StreamAssistantReplyUseCase
import com.example.stugbygget.domain.usecase.PlanLogisticsWithRouteUseCase
import com.example.stugbygget.domain.usecase.RunNotificationPipelineUseCase
import com.example.stugbygget.domain.usecase.BuildNotificationEventsUseCase
import com.example.stugbygget.domain.usecase.DispatchNotificationEventsUseCase
import com.example.stugbygget.domain.usecase.FetchRuntimeConfigUseCase
import com.example.stugbygget.domain.usecase.UpdateNotificationSettingsUseCase
import com.example.stugbygget.domain.usecase.ToggleShoppingItemPurchasedUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import com.example.stugbygget.domain.usecase.UploadPhotoUseCase
import com.example.stugbygget.domain.usecase.UpsertTodoUseCase
import com.example.stugbygget.feature.roomplanner.ui.defaultFurniture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(
    appContext: Context
) {
    val applicationContext: Context = appContext.applicationContext

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }
    val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(firebaseAuth) }
    val phaseRepository: PhaseRepository by lazy { FirestorePhaseRepository(firestore) }
    val todoRepository: TodoRepository by lazy { FirestoreTodoRepository(firestore) }
    val shoppingRepository: ShoppingRepository by lazy { FirestoreShoppingRepository(firestore) }
    val budgetRepository: BudgetRepository by lazy { FirestoreBudgetRepository(firestore) }
    val logisticsRepository: LogisticsRepository by lazy { FirestoreLogisticsRepository(firestore) }
    val photoRepository: PhotoRepository by lazy { FirestorePhotoRepository(firestore, storage) }
    val measurementRepository: MeasurementRepository by lazy { FirestoreMeasurementRepository(firestore) }
    val priceRecommendationRepository: PriceRecommendationRepository by lazy {
        FirestorePriceRecommendationRepository(firestore)
    }
    val projectContextProvider: FirestoreProjectContextProvider by lazy {
        FirestoreProjectContextProvider(firestore)
    }
    val runtimeConfigRepository: RuntimeConfigRepository by lazy {
        FirebaseRuntimeConfigRepository(remoteConfig)
    }
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.CLAUDE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val mapsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val claudeApiService: ClaudeApiService by lazy {
        retrofit.create(ClaudeApiService::class.java)
    }
    val googleDirectionsService: GoogleDirectionsService by lazy {
        mapsRetrofit.create(GoogleDirectionsService::class.java)
    }
    val chatRepository: ChatRepository by lazy {
        ClaudeChatRepository(
            apiService = claudeApiService,
            contextProvider = projectContextProvider
        )
    }
    val roomLayoutDataSource: LocalRoomLayoutDataSource by lazy {
        LocalRoomLayoutDataSource(applicationContext)
    }
    val routeCacheDataSource: RouteCacheDataSource by lazy {
        RouteCacheDataSource(applicationContext)
    }
    val roomDimensionsRepository: RoomDimensionsRepository by lazy {
        LocalRoomDimensionsRepository(applicationContext)
    }
    val notificationSettingsRepository: NotificationSettingsRepository by lazy {
        LocalNotificationSettingsRepository(applicationContext)
    }
    val notificationDispatchGateway: NotificationDispatchGateway by lazy {
        AndroidNotificationDispatcher(applicationContext)
    }
    val roomLayoutRepository: RoomLayoutRepository by lazy {
        LocalRoomLayoutRepository(
            dataSource = roomLayoutDataSource,
            defaultLayoutProvider = { defaultFurniture() }
        )
    }
    val routeRepository: RouteRepository by lazy {
        GoogleRouteRepository(
            service = googleDirectionsService,
            cacheDataSource = routeCacheDataSource
        )
    }

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
    val calculateLogisticsRecommendationUseCase: CalculateLogisticsRecommendationUseCase by lazy {
        CalculateLogisticsRecommendationUseCase(logisticsRepository)
    }
    val getRouteMetricsUseCase: GetRouteMetricsUseCase by lazy {
        GetRouteMetricsUseCase(routeRepository)
    }
    val planLogisticsWithRouteUseCase: PlanLogisticsWithRouteUseCase by lazy {
        PlanLogisticsWithRouteUseCase(
            getRouteMetricsUseCase = getRouteMetricsUseCase,
            calculateLogisticsRecommendationUseCase = calculateLogisticsRecommendationUseCase
        )
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
    val streamAssistantReplyUseCase: StreamAssistantReplyUseCase by lazy {
        StreamAssistantReplyUseCase(chatRepository)
    }
    val observeRoomLayoutUseCase: ObserveRoomLayoutUseCase by lazy {
        ObserveRoomLayoutUseCase(roomLayoutRepository)
    }
    val saveRoomLayoutUseCase: SaveRoomLayoutUseCase by lazy {
        SaveRoomLayoutUseCase(roomLayoutRepository)
    }
    val getRoomDimensionsUseCase: GetRoomDimensionsUseCase by lazy {
        GetRoomDimensionsUseCase(roomDimensionsRepository)
    }
    val moveFurnitureUseCase: MoveFurnitureUseCase by lazy {
        MoveFurnitureUseCase()
    }
    val measurementUnitConverter: MeasurementUnitConverter by lazy {
        MeasurementUnitConverter()
    }
    val calculateMeasurementDistanceUseCase: CalculateMeasurementDistanceUseCase by lazy {
        CalculateMeasurementDistanceUseCase()
    }
    val saveMeasurementUseCase: SaveMeasurementUseCase by lazy {
        SaveMeasurementUseCase(measurementRepository, measurementUnitConverter)
    }
    val exportMeasurementToRoomPlannerUseCase: ExportMeasurementToRoomPlannerUseCase by lazy {
        ExportMeasurementToRoomPlannerUseCase(roomDimensionsRepository, measurementUnitConverter)
    }
    val fetchRuntimeConfigUseCase: FetchRuntimeConfigUseCase by lazy {
        FetchRuntimeConfigUseCase(runtimeConfigRepository)
    }
    val getRuntimeConfigUseCase: GetRuntimeConfigUseCase by lazy {
        GetRuntimeConfigUseCase(runtimeConfigRepository)
    }
}
