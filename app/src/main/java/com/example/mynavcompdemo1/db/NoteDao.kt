package com.example.mynavcompdemo1.db

import androidx.lifecycle.LiveData
import androidx.room.*

// 1 Data Access Object per entity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // it performs an update if it exists
    fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes ORDER BY date ASC")
    fun getAll(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id") // :id refers to the parameter
    fun getNoteById(id: Int): NoteEntity?

    @Query("SELECT COUNT(*) from notes")
    fun getCount(): Int

    @Delete
    fun deleteNotes(selectedNotes: List<NoteEntity>): Int

    @Query("DELETE FROM notes")
    fun deleteAll():Int

    @Delete
    fun deleteNote(note: NoteEntity)

}