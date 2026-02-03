package dev.wondertech.notedup.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.wondertech.notedup.common.DeleteConfirmationDialog
import dev.wondertech.notedup.common.NotedUpTopAppBar
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.modal.NoteData
import kotlinx.coroutines.launch
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.edit_icon
import notedup.composeapp.generated.resources.no_task
import org.jetbrains.compose.resources.painterResource

class NotesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val databaseHelper = LocalDatabase.current
        val coroutineScope = rememberCoroutineScope()

        var notes by remember { mutableStateOf<List<NoteData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var noteToDelete by remember { mutableStateOf<NoteData?>(null) }

        LaunchedEffect(navigator.lastItem) {
            try {
                isLoading = true
                notes = databaseHelper.getAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
                notes = emptyList()
            } finally {
                isLoading = false
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navigator.push(CreateNoteScreen())
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.edit_icon),
                        contentDescription = "Create Note",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                NotedUpTopAppBar(
                    title = "Your notes",
                    canShowNavigationIcon = true,
                    onBackButtonClick = {
                        navigator.pop()
                    }
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading notes...",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.no_task),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notes yet\ntap + to create one",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notes, key = { it.timestampMillis }) { note ->
                            NoteCard(
                                noteData = note,
                                onClick = {
                                    navigator.push(CreateNoteScreen(noteTimestampToEdit = note.timestampMillis))
                                },
                                onLongClick = {
                                    noteToDelete = note
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        DeleteConfirmationDialog(
            showDialog = showDeleteDialog,
            taskTitle = noteToDelete?.title?.ifEmpty { "this note" } ?: "Note",
            itemType = "Note",
            onConfirm = {
                noteToDelete?.let { note ->
                    coroutineScope.launch {
                        try {
                            databaseHelper.deleteNote(note.timestampMillis)
                            notes = notes.filter { it.timestampMillis != note.timestampMillis }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                showDeleteDialog = false
                noteToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                noteToDelete = null
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    noteData: NoteData,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { onLongClick() }
                ),
            border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = noteData.title.ifEmpty { "Untitled" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (noteData.content.isNotBlank()) {
                    Text(
                        text = noteData.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = noteData.formattedDate,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
