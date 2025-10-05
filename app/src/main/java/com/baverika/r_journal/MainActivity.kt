// app/src/main/java/com/baverika/r_journal/MainActivity.kt

package com.baverika.r_journal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.baverika.r_journal.data.local.database.JournalDatabase
import com.baverika.r_journal.repository.JournalRepository
import com.baverika.r_journal.repository.QuickNoteRepository
import com.baverika.r_journal.ui.screens.*
import com.baverika.r_journal.ui.theme.RJournalTheme
import com.baverika.r_journal.ui.viewmodel.JournalViewModelFactory
import com.baverika.r_journal.ui.viewmodel.QuickNoteViewModelFactory
import com.baverika.r_journal.ui.viewmodel.SearchViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = JournalDatabase.getDatabase(this)
        val journalRepo = JournalRepository(db.journalDao())
        val quickNoteRepo = QuickNoteRepository(db.quickNoteDao())

        setContent {
            RJournalTheme {
                MainApp(journalRepo, quickNoteRepo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    journalRepo: JournalRepository,
    quickNoteRepo: QuickNoteRepository
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Journal) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Outer NavController for top-level navigation
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DrawerContent(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                        scope.launch { drawerState.close() }
                    }
                )
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("r_journal") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentScreen is Screen.Journal) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("chat_input") // Navigate to today's chat
                        },
                        content = {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = "New Journal Entry"
                            )
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                // Inner NavHost for main content area
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    // Main content area route
                    composable("main") {
                        when (currentScreen) {
                            is Screen.Journal -> {
                                JournalArchiveScreen(
                                    journalRepo = journalRepo,
                                    onEntryClick = { entry ->
                                        navController.navigate("chat_input/${entry.id}") // Navigate to specific entry
                                    }
                                )
                            }

                            is Screen.QuickNotes -> {
                                QuickNotesScreen(
                                    viewModel = viewModel(
                                        factory = QuickNoteViewModelFactory(
                                            quickNoteRepo,
                                            context
                                        )
                                    ),
                                    navController = navController
                                )
                            }

                            is Screen.Search -> {
                                SearchScreen(
                                    viewModel = viewModel(
                                        factory = SearchViewModelFactory(
                                            journalRepo,
                                            context
                                        )
                                    ),
                                    navController = navController
                                )
                            }

                            is Screen.Dashboard -> {
                                DashboardScreen(journalRepo)
                            }

                            is Screen.Export -> {
                                ExportScreen(
                                    journalRepo = journalRepo,
                                    quickNoteRepo = quickNoteRepo,
                                    context = context
                                )
                            }

                            is Screen.Import -> {
                                ImportScreen(
                                    journalRepo = journalRepo,
                                    quickNoteRepo = quickNoteRepo
                                )
                            }

                            else -> {
                                // Fallback: Show a simple text or navigate to default screen
                                Text("Unknown screen: $currentScreen")
                            }
                        }

                        // Nested screens
                        composable("chat_input") {
                            val journalViewModel: com.baverika.r_journal.ui.viewmodel.JournalViewModel =
                                viewModel(
                                    factory = JournalViewModelFactory(journalRepo, context)
                                )
                            ChatInputScreen(
                                viewModel = journalViewModel,
                                navController = navController,
                                entryIdToLoad = null // Load today's entry
                            )
                        }

                        composable("chat_input/{entryId}") { backStackEntry ->
                            val entryId = backStackEntry.arguments?.getString("entryId")
                            if (entryId != null) {
                                val journalViewModel: com.baverika.r_journal.ui.viewmodel.JournalViewModel =
                                    viewModel(
                                        factory = JournalViewModelFactory(journalRepo, context)
                                    )
                                LaunchedEffect(entryId) {
                                    journalViewModel.loadEntryForEditing(entryId)
                                }
                                ChatInputScreen(
                                    viewModel = journalViewModel,
                                    navController = navController,
                                    entryIdToLoad = entryId
                                )
                            } else {
                                navController.popBackStack()
                            }
                        }

                        composable("new_quick_note") {
                            val quickNoteViewModel: com.baverika.r_journal.ui.viewmodel.QuickNoteViewModel =
                                viewModel(
                                    factory = QuickNoteViewModelFactory(quickNoteRepo, context)
                                )
                            NewQuickNoteScreen(
                                viewModel = quickNoteViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DrawerContent(
        currentScreen: Screen,
        onScreenSelected: (Screen) -> Unit
    ) {
        Column {
            DrawerItem(
                icon = Icons.Filled.Chat,
                label = "Journal",
                isSelected = currentScreen == Screen.Journal,
                onClick = { onScreenSelected(Screen.Journal) }
            )
            DrawerItem(
                icon = Icons.Filled.Note,
                label = "Quick Notes",
                isSelected = currentScreen == Screen.QuickNotes,
                onClick = { onScreenSelected(Screen.QuickNotes) }
            )
            DrawerItem(
                icon = Icons.Filled.Search,
                label = "Search",
                isSelected = currentScreen == Screen.Search,
                onClick = { onScreenSelected(Screen.Search) }
            )
            DrawerItem(
                icon = Icons.Filled.BarChart,
                label = "Dashboard",
                isSelected = currentScreen == Screen.Dashboard,
                onClick = { onScreenSelected(Screen.Dashboard) }
            )
            DrawerItem(
                icon = Icons.Filled.Upload,
                label = "Export All",
                isSelected = currentScreen == Screen.Export,
                onClick = { onScreenSelected(Screen.Export) }
            )
            DrawerItem(
                icon = Icons.Filled.Download,
                label = "Import",
                isSelected = currentScreen == Screen.Import,
                onClick = { onScreenSelected(Screen.Import) }
            )
        }
    }

    @Composable
    fun DrawerItem(
        icon: ImageVector,
        label: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        NavigationDrawerItem(
            icon = { Icon(icon, contentDescription = null) },
            label = { Text(label) },
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}