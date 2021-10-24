package com.example.mynavcompdemo1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mynavcompdemo1.db.AppDatabase
import com.example.mynavcompdemo1.db.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val database = AppDatabase.getInstance(app)
    var currentNote : MutableLiveData<NoteEntity>? = MutableLiveData<NoteEntity>()

    var listNotes = MutableLiveData<List<NoteEntity>>()

    fun getNoteById(noteId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val note =
                    if (noteId != NEW_NOTE_ID) {
                        database?.noteDao()?.getNoteById(noteId)
                    } else {
                        // returns an empty NoteEntity if it is the very first note to be created
                        NoteEntity()
                    }

                listNotes = MutableLiveData<List<NoteEntity>>(database?.noteDao()?.getAll()?.value)

                currentNote?.postValue(note)  // use postValue because it is running in the background
            }
        }
    }

    fun updateNote() {
        // would only be evaluated if currentNote is not null
        currentNote?.value?.let {
            it.text = it.text.trim()
            if (it.id == NEW_NOTE_ID && it.text.isEmpty()) {
                return
            }

            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    if (it.text.isEmpty()) {
                        database?.noteDao()?.deleteNote(it)
                    } else {
                        // if it exists, it will perform an update
                        // see NoteDao for the implementation details
                        database?.noteDao()?.insertNote(it)
                    }
                }
            }

        }


    }

}