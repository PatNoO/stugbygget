package com.example.stugbygget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.feature.auth.ui.AuthViewModel
import com.example.stugbygget.feature.auth.ui.AuthViewModelFactory
import com.example.stugbygget.feature.auth.ui.SignInScreen
import com.example.stugbygget.navigation.AppNavHost
import com.example.stugbygget.navigation.primaryRoutes
import com.example.stugbygget.ui.theme.StugbyggetTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as StugByggetApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StugbyggetTheme {
                AppContent(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
private fun AppContent(authViewModel: AuthViewModel) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val container = (context.applicationContext as StugByggetApp).container

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            authViewModel.onGoogleSignInFailed("No sign-in result returned.")
            return@rememberLauncherForActivityResult
        }

        runCatching {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        }.onSuccess { account ->
            val token = account.idToken
            if (token.isNullOrBlank()) {
                authViewModel.onGoogleSignInFailed("Missing Google ID token.")
            } else {
                authViewModel.onGoogleTokenReceived(token)
            }
        }.onFailure {
            authViewModel.onGoogleSignInFailed(it.message ?: "Google sign-in failed.")
        }
    }

    if (authState.currentUser == null) {
        SignInScreen(
            isLoading = authState.isLoading,
            errorMessage = authState.errorMessage,
            onSignInClick = {
                val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                if (webClientId.isBlank()) {
                    authViewModel.onGoogleSignInFailed(
                        "GOOGLE_WEB_CLIENT_ID is missing. Set it in local.properties."
                    )
                } else {
                    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                    val client = GoogleSignIn.getClient(context, options)
                    launcher.launch(client.signInIntent)
                }
            }
        )
    } else {
        MainNavigationScaffold(container = container)
    }
}

@Composable
private fun MainNavigationScaffold(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val syncState by container.offlineSyncCoordinator.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (!syncState.isOnline || syncState.pendingWrites > 0 || syncState.isSyncing) {
                val text = when {
                    !syncState.isOnline -> "Offline mode: writes will sync when online."
                    syncState.isSyncing -> "Syncing pending changes..."
                    else -> "Pending writes: ${syncState.pendingWrites}"
                }
                Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                primaryRoutes.take(5).forEach { route ->
                    NavigationBarItem(
                        selected = currentRoute == route.route,
                        onClick = { navController.navigate(route.route) },
                        icon = { Text(route.title.take(1)) },
                        label = { Text(route.title) }
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
