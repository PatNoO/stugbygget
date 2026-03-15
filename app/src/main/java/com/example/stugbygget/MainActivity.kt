package com.example.stugbygget

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.feature.auth.ui.AuthViewModel
import com.example.stugbygget.feature.auth.ui.AuthViewModelFactory
import com.example.stugbygget.feature.auth.ui.SignInScreen
import com.example.stugbygget.navigation.AppNavHost
import com.example.stugbygget.navigation.primaryRoutes
import com.example.stugbygget.ui.components.SommarTopBar
import com.example.stugbygget.ui.theme.StugbyggetTheme

class MainActivity : ComponentActivity() {

    /** Route to navigate to after launch (set by notification tap). */
    var pendingNavigationRoute by mutableStateOf<String?>(null)
        private set

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — no crash either way */ }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as StugByggetApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNavigationRoute = intent.getStringExtra(EXTRA_NAVIGATE_TO)
        requestNotificationPermissionIfNeeded()

        setContent {
            StugbyggetTheme {
                AppContent(
                    authViewModel = authViewModel,
                    pendingNavigationRoute = pendingNavigationRoute,
                    onNavigationConsumed = { pendingNavigationRoute = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingNavigationRoute = intent.getStringExtra(EXTRA_NAVIGATE_TO)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
    }
}

@Composable
private fun AppContent(
    authViewModel: AuthViewModel,
    pendingNavigationRoute: String?,
    onNavigationConsumed: () -> Unit,
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val container = (context.applicationContext as StugByggetApp).container

    if (authState.currentUser == null) {
        SignInScreen(
            email = authState.email,
            password = authState.password,
            isLoading = authState.isLoading,
            errorMessage = authState.errorMessage,
            onEmailChanged = authViewModel::onEmailChanged,
            onPasswordChanged = authViewModel::onPasswordChanged,
            onSignInClick = authViewModel::onEmailPasswordSignIn
        )
    } else {
        MainNavigationScaffold(
            container = container,
            onSignOut = authViewModel::signOut,
            pendingNavigationRoute = pendingNavigationRoute,
            onNavigationConsumed = onNavigationConsumed,
        )
    }
}

@Composable
private fun MainNavigationScaffold(
    container: AppContainer,
    onSignOut: () -> Unit,
    pendingNavigationRoute: String?,
    onNavigationConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val syncState by container.offlineSyncCoordinator.state.collectAsStateWithLifecycle()

    // Handle deep-link from notification tap
    LaunchedEffect(pendingNavigationRoute) {
        val route = pendingNavigationRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        onNavigationConsumed()
    }

    Scaffold(
        topBar = {
            Column {
                SommarTopBar(
                    trailing = {
                        TextButton(onClick = onSignOut) {
                            Text("Sign out")
                        }
                    }
                )
                if (!syncState.isOnline || syncState.pendingWrites > 0 || syncState.isSyncing) {
                    val text = when {
                        !syncState.isOnline -> "Offline mode: writes will sync when online."
                        syncState.isSyncing -> "Syncing pending changes..."
                        else -> "Pending writes: ${syncState.pendingWrites}"
                    }
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            val selectedIndex = primaryRoutes.indexOfFirst { route -> route.route == currentRoute }
            ScrollableTabRow(
                selectedTabIndex = selectedIndex.coerceAtLeast(0)
            ) {
                primaryRoutes.forEach { route ->
                    Tab(
                        selected = currentRoute == route.route,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        text = { Text(route.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            container = container,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
