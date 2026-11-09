package victor.android.textify.ui.notes

import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import victor.android.textify.R
import victor.android.textify.domain.model.Note
import victor.android.textify.ui.theme.TextifyTheme
import victor.android.textify.util.createShareTextIntent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotesScreen(
    onNavigateToRecord: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.notes_deleted_message)
    val undoLabel = stringResource(R.string.notes_undo_button)
    val copiedMessage = stringResource(R.string.notes_copy_success)

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                is NotesEvent.NoteDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = deletedMessage,
                        actionLabel = undoLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoDelete(event.note)
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        NotesScreen(
            uiState = uiState,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onNavigateToRecord = onNavigateToRecord,
            onDeleteNote = viewModel::onDeleteNote,
            onShareNote = { note ->
                context.startActivity(Intent.createChooser(createShareTextIntent(note.text), null))
            },
            onCopyNote = { note ->
                clipboardManager.setText(AnnotatedString(note.text))
                scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
            },
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun NotesScreen(
    uiState: NotesUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToRecord: () -> Unit,
    onDeleteNote: (Note) -> Unit,
    onShareNote: (Note) -> Unit,
    onCopyNote: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.notes_search_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        when (uiState) {
            is NotesUiState.Loading -> LoadingContent(modifier = Modifier.fillMaxSize())
            is NotesUiState.Empty -> EmptyNotesContent(
                onNavigateToRecord = onNavigateToRecord,
                modifier = Modifier.fillMaxSize(),
            )
            is NotesUiState.Content -> NotesList(
                notes = uiState.notes,
                onNoteClick = { selectedNote = it },
                onShareNote = onShareNote,
                onDeleteNote = onDeleteNote,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    val noteToShow = selectedNote
    if (noteToShow != null) {
        NoteDetailDialog(
            note = noteToShow,
            onDismiss = { selectedNote = null },
            onShare = { onShareNote(noteToShow) },
            onCopy = { onCopyNote(noteToShow) },
            onDelete = {
                onDeleteNote(noteToShow)
                selectedNote = null
            },
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyNotesContent(onNavigateToRecord: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.notes_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToRecord) {
            Text(stringResource(R.string.notes_empty_record_button))
        }
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onShareNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(notes, key = { it.id }) { note ->
            NoteListItem(
                note = note,
                onClick = { onNoteClick(note) },
                onShare = { onShareNote(note) },
                onDelete = { onDeleteNote(note) },
            )
        }
    }
}

private val SwipeActionWidth = 72.dp
private val SwipeActionsWidth = SwipeActionWidth * 2

@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    var revealed by remember(note.id) { mutableStateOf(false) }
    val offsetX by animateDpAsState(targetValue = if (revealed) -SwipeActionsWidth else 0.dp, label = "note_swipe_offset")
    val dragThresholdPx = with(LocalDensity.current) { 2.dp.toPx() }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            SwipeActionButton(
                icon = Icons.Filled.Share,
                contentDescription = stringResource(R.string.notes_share_description),
                onClick = {
                    revealed = false
                    onShare()
                },
            )
            SwipeActionButton(
                icon = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.notes_delete_description),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = {
                    revealed = false
                    onDelete()
                },
            )
        }
        Card(
            onClick = { if (revealed) revealed = false else onClick() },
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX)
                .pointerInput(note.id) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -dragThresholdPx) revealed = true
                        else if (dragAmount > dragThresholdPx) revealed = false
                    }
                },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = formatNoteDate(note.createdAt), style = MaterialTheme.typography.labelMedium)
                Text(text = note.text, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
            }
        }
    }
}

@Composable
private fun SwipeActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        modifier = Modifier.fillMaxHeight().width(SwipeActionWidth),
        contentAlignment = Alignment.Center,
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Composable
private fun NoteDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatNoteDate(note.createdAt)) },
        text = { Text(note.text) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.notes_share_description))
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.notes_copy_description),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.notes_delete_description))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.notes_close_button))
            }
        },
    )
}

private val noteDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

private fun formatNoteDate(instant: Instant): String =
    noteDateFormatter.withZone(ZoneId.systemDefault()).format(instant)

@Preview(showBackground = true)
@Composable
private fun NotesScreenEmptyPreview() {
    TextifyTheme { NotesScreen(NotesUiState.Empty, "", {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenContentPreview() {
    TextifyTheme {
        NotesScreen(
            uiState = NotesUiState.Content(
                listOf(
                    Note(id = 1, text = "Comprar leche", createdAt = Instant.now()),
                    Note(id = 2, text = "Llamar al doctor", createdAt = Instant.now()),
                ),
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onNavigateToRecord = {},
            onDeleteNote = {},
            onShareNote = {},
            onCopyNote = {},
        )
    }
}
