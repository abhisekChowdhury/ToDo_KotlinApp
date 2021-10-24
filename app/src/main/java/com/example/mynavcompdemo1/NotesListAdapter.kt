package com.example.mynavcompdemo1
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mynavcompdemo1.R
import com.example.mynavcompdemo1.databinding.ListItemBinding
import com.example.mynavcompdemo1.db.NoteEntity

class NotesListAdapter(private val notesList: List<NoteEntity>,
                       private val listener: ListItemListener
) :
    RecyclerView.Adapter<NotesListAdapter.ViewHolder>() {

    val selectedNotes = arrayListOf<NoteEntity>()

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding = ListItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]
        with(holder.binding) {
            theNoteTxt.text = note.text
            root.setOnClickListener{
                listener.editNote(note.id)
            }

            // fab is defined in list_item.xml
            // note refers to the current "note" row
            fab.setOnClickListener{
                if (selectedNotes.contains(note)) {
                    selectedNotes.remove(note)
                    fab.setImageResource(R.drawable.ic_notes)
                } else {
                    selectedNotes.add(note)
                    fab.setImageResource(R.drawable.ic_check)
                }

                // informs the listener (implemented in MainFragment) that something
                // has changed
                listener.onItemSelectionChanged()
            }

            fab.setImageResource(
                if (selectedNotes.contains(note)) {
                    R.drawable.ic_check
                } else {
                    R.drawable.ic_notes
                }
            )
        }
    }

    // implemented in MainFragment
    interface ListItemListener {
        fun editNote(noteId: Int)
        fun onItemSelectionChanged()
    }
}