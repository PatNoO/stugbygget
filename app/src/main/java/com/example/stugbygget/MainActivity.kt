package com.example.stugbygget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.stugbygget.navigation.AppRoute
import com.example.stugbygget.ui.components.SommarTopBar
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.CreamBackground
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.StugbyggetTheme
import com.example.stugbygget.ui.theme.TextMedium

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
            onSignOut = authViewModel::signOut
        )
    }
}

private data class NavItem(val emoji: String, val label: String, val route: String)

private val primaryNavItems = listOf(
    NavItem("📅", "Plan", AppRoute.Planning.route),
    NavItem("✅", "Todos", AppRoute.Todos.route),
    NavItem("📸", "Photos", AppRoute.Gallery.route),
    NavItem("🤖", "AI", AppRoute.AiChat.route),
)

private val moreNavItems = listOf(
    NavItem("🔧", "Room Planner", AppRoute.RoomPlanner.route),
    NavItem("📐", "Measure", AppRoute.ArMeasure.route),
    NavItem("🔗", "Materials", AppRoute.Materials.route),
    NavItem("📊", "Budget", AppRoute.Budget.route),
    NavItem("🚛", "Transport", AppRoute.Logistics.route),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigationScaffold(
    container: AppContainer,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val syncState by container.offlineSyncCoordinator.state.collectAsStateWithLifecycle()

    var showMoreSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isMoreSelected = moreNavItems.any { it.route == currentRoute }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
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
            SommarBottomBar(
                currentRoute = currentRoute,
                isMoreSelected = isMoreSelected,
                onTabClick = { route -> navigateTo(route) },
                onMoreClick = { showMoreSheet = true },
            )
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            container = container,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = CreamBackground,
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
                moreNavItems.forEach { item ->
                    MoreSheetItem(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = {
                            showMoreSheet = false
                            navigateTo(item.route)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SommarBottomBar(
    currentRoute: String?,
    isMoreSelected: Boolean,
    onTabClick: (String) -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamBackground)
            .border(width = 1.dp, color = Border)
            .navigationBarsPadding()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        primaryNavItems.forEach { item ->
            NavTabItem(
                emoji = item.emoji,
                label = item.label,
                selected = currentRoute == item.route,
                modifier = Modifier.weight(1f),
                onClick = { onTabClick(item.route) },
            )
        }
        NavTabItem(
            emoji = "☰",
            label = "More",
            selected = isMoreSelected,
            modifier = Modifier.weight(1f),
            onClick = onMoreClick,
        )
    }
}

@Composable
private fun NavTabItem(
    emoji: String,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) FaluRed else TextMedium

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = contentColor),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(if (selected) 4.dp else 0.dp)
                .clip(CircleShape)
                .background(FaluRed)
        )
    }
}

@Composable
private fun MoreSheetItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) FaluRed.copy(alpha = 0.06f) else CreamBackground)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(item.emoji, style = MaterialTheme.typography.titleMedium)
        Text(
            text = item.label,
            style = MonoStyles.dataSmall.copy(
                color = if (selected) FaluRed else TextMedium,
            ),
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(FaluRed)
            )
        }
    }
}
