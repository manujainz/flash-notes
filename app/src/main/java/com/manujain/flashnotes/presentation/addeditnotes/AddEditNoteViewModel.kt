package com.manujain.flashnotes.presentation.addeditnotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manujain.flashnotes.core.isDiff
import com.manujain.flashnotes.domain.model.InvalidNoteException
import com.manujain.flashnotes.domain.model.Note
import com.manujain.flashnotes.domain.model.Note.Companion.NOTE_ID
import com.manujain.flashnotes.domain.usecase.NotesUsecase
import com.manujain.flashnotes.domain.utils.AddEditNoteUiEvent
import com.manujain.flashnotes.domain.utils.NoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesUseCase: NotesUsecase,
) : ViewModel() {

    private val _noteState = MutableStateFlow(NoteState(isLoading = true))
    val noteState = _noteState.asStateFlow()

    private var currentNote: Note? = null

    init {
        val noteId: String? = savedStateHandle[NOTE_ID]
        if (noteId != null) {
            viewModelScope.launch {
                notesUseCase.getNote(noteId).also { note ->
                    currentNote = note
                    _noteState.value = noteState.value.copy(
                        title = note.title,
                        color = note.color,
                        content = note.content,
                        isLoading = false
                    )
                }
            }
        } else {
            _noteState.value = NoteState()
        }
    }

    fun onEvent(event: AddEditNoteUiEvent) {
        when (event) {
            is AddEditNoteUiEvent.DeleteNote -> {}
            is AddEditNoteUiEvent.OnColorChange -> _noteState.value = noteState.value.copy(color = event.color)
            is AddEditNoteUiEvent.OnTitleChange -> _noteState.value = noteState.value.copy(title = event.title)
            is AddEditNoteUiEvent.OnContentChange -> _noteState.value = noteState.value.copy(content = event.content)
            is AddEditNoteUiEvent.AddNote -> {
                if (currentNote.isDiff(noteState.value)) {
                    addNote(noteState.value, currentNote)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun addNote(noteState: NoteState, currentNote: Note? = null) {
        // Need to explore better approach than launching in global scope
        GlobalScope.launch {
            try {
                notesUseCase.addNote(
                    Note(
                        title = noteState.title,
                        content = noteState.content,
                        color = noteState.color,
                        timestamp = currentNote?.timestamp ?: System.currentTimeMillis(),
                        id = currentNote?.id ?: UUID.randomUUID().toString()
                    )
                )
            } catch (e: InvalidNoteException) {
                Timber.e(e, e.message)
            }
        }
    }
}
