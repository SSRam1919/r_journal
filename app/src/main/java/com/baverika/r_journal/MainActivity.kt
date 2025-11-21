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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Track current route for FAB visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "R-Journal",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                    )

                    Divider()

                    DrawerContent(
                        currentRoute = currentRoute,
                        onScreenSelected = { route ->
                            navController.navigate(route) {
                                popUpTo("archive") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
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
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Navigation content
                NavHost(
                    navController = navController,
                    startDestination = "archive"
                ) {
                    // Archive screen (default/home)
                    composable("archive") {
                        JournalArchiveScreen(
                            journalRepo = journalRepo,
                            onEntryClick = { entry ->
                                navController.navigate("chat_input/${entry.id}")
                            }
                        )
                    }

                    // Quick Notes
                    composable("quick_notes") {
                        QuickNotesScreen(
                            viewModel = viewModel(
                                factory = QuickNoteViewModelFactory(quickNoteRepo, context)
                            ),
                            navController = navController
                        )
                    }

                    // Search
                    composable("search") {
                        SearchScreen(
                            viewModel = viewModel(
                                factory = SearchViewModelFactory(journalRepo, context)
                            ),
                            navController = navController
                        )
                    }

                    // Dashboard
                    composable("dashboard") {
                        DashboardScreen(journalRepo)
                    }

                    // Export
                    composable("export") {
                        ExportScreen(
                            journalRepo = journalRepo,
                            quickNoteRepo = quickNoteRepo,
                            context = context
                        )
                    }

                    // Import
                    composable("import") {
                        ImportScreen(
                            journalRepo = journalRepo,
                            quickNoteRepo = quickNoteRepo
                        )
                    }

                    // Chat input for today's entry
                    composable("chat_input") {
                        val journalViewModel: com.baverika.r_journal.ui.viewmodel.JournalViewModel =
                            viewModel(factory = JournalViewModelFactory(journalRepo, context))

                        LaunchedEffect(Unit) {
                            journalViewModel.loadTodaysEntry()
                        }

                        ChatInputScreen(
                            viewModel = journalViewModel,
                            navController = navController
                        )
                    }

                    // Chat input for specific entry by ID
                    composable("chat_input/{entryId}") { backStackEntry ->
                        val entryId = backStackEntry.arguments?.getString("entryId")
                        if (entryId != null) {
                            val journalViewModel: com.baverika.r_journal.ui.viewmodel.JournalViewModel =
                                viewModel(factory = JournalViewModelFactory(journalRepo, context))

                            LaunchedEffect(entryId) {
                                journalViewModel.loadEntryForEditing(entryId)
                            }

                            ChatInputScreen(
                                viewModel = journalViewModel,
                                navController = navController
                            )
                        } else {
                            // Invalid entry ID, go back to archive
                            LaunchedEffect(Unit) {
                                navController.navigate("archive") {
                                    popUpTo("archive") { inclusive = true }
                                }
                            }
                        }
                    }

                    // New quick note screen
                    composable("new_quick_note") {
                        val quickNoteViewModel: com.baverika.r_journal.ui.viewmodel.QuickNoteViewModel =
                            viewModel(factory = QuickNoteViewModelFactory(quickNoteRepo, context))

                        NewQuickNoteScreen(
                            viewModel = quickNoteViewModel,
                            navController = navController
                        )
                    }

                    // Image viewer screen
                    // Image viewer screen
                    composable("image_viewer/{encodedPath}") { backStackEntry ->
                        val encodedPath = backStackEntry.arguments?.getString("encodedPath")
                        encodedPath?.let {
                            val decodedPath = remember(it) {
                                try {
                                    java.net.URLDecoder.decode(it, "UTF-8")
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            when {
                                decodedPath != null -> {
                                    ImageViewerScreen(
                                        imageUri = decodedPath,
                                        onDismiss = { navController.popBackStack() }
                                    )
                                }
                                else -> {
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentRoute == "archive") {
                    FloatingActionButton(
                        onClick = { navController.navigate("chat_input") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 36.dp, bottom = 80.dp)
                            .size(72.dp)
                    ) {
                        Icon(
                            Icons.Filled.Chat,
                            contentDescription = "New Journal Entry",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentRoute: String?,
    onScreenSelected: (String) -> Unit
) {
    Column {
        DrawerItem(
            icon = Icons.Filled.Book,
            label = "Journal Archive",
            isSelected = currentRoute == "archive",
            onClick = { onScreenSelected("archive") }
        )
        DrawerItem(
            icon = Icons.Filled.Note,
            label = "Quick Notes",
            isSelected = currentRoute == "quick_notes",
            onClick = { onScreenSelected("quick_notes") }
        )
        DrawerItem(
            icon = Icons.Filled.Search,
            label = "Search",
            isSelected = currentRoute == "search",
            onClick = { onScreenSelected("search") }
        )
        DrawerItem(
            icon = Icons.Filled.BarChart,
            label = "Dashboard",
            isSelected = currentRoute == "dashboard",
            onClick = { onScreenSelected("dashboard") }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            icon = Icons.Filled.Upload,
            label = "Export Data",
            isSelected = currentRoute == "export",
            onClick = { onScreenSelected("export") }
        )
        DrawerItem(
            icon = Icons.Filled.Download,
            label = "Import Data",
            isSelected = currentRoute == "import",
            onClick = { onScreenSelected("import") }
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