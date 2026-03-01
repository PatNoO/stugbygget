package com.example.stugbygget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            authViewModel.onGoogleSignInFailed("Inget inloggningsresultat")
            return@rememberLauncherForActivityResult
        }

        runCatching {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        }.onSuccess { account ->
            val token = account.idToken
            if (token.isNullOrBlank()) {
                authViewModel.onGoogleSignInFailed("Saknar Google ID-token")
            } else {
                authViewModel.onGoogleTokenReceived(token)
            }
        }.onFailure {
            authViewModel.onGoogleSignInFailed(it.message ?: "Google-inloggning misslyckades")
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
                        "GOOGLE_WEB_CLIENT_ID saknas. Sätt värdet i local.properties"
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
        MainNavigationScaffold()
    }
}

@Composable
private fun MainNavigationScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}
