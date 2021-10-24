package com.example.mynavcompdemo1

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynavcompdemo1.R
import com.example.mynavcompdemo1.databinding.MainFragmentBinding
import com.example.mynavcompdemo1.db.NoteEntity

/*
    Step 2
    1. need references to
       a. MainViewModel : contains access to the database and noteList (declared as LiveData<List<NoteEntity>>)
          See : AppDatabase and NoteDao.kt
       b. MainFragmentBinding: refers to main_fragment.xml (User Interface)
          RecyclerView (android:id="@+id/recyclerView") and floatingActionButton (android:id="@+id/floatingActionButton")
       c. NoteListAdapter :
          -- handles the RecyclerView's display
          -- floatingActionButton : it is used to add and remove notes from an array (selectedNotes = arrayListOf<NoteEntity>())
          -- interface ListItemListener which is implemented in MainFragment (here)
          -- examine onBindViewHolder. See how editNote and onItemSelectionChanged() are called

     2. See the editNote and onItemSelectionChanged implementation (below)

     Go to EditorViewModel

 */
class MainFragment : Fragment(),
    NotesListAdapter.ListItemListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding : MainFragmentBinding // name convention: name of the class + Binding
    private lateinit var adapter: NotesListAdapter   // the instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // to "hide" the icon on the Home
        (activity as AppCompatActivity)
            .supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setHasOptionsMenu(true) // to show the ... on the upper right

        requireActivity().title = "Note Taking in Kotlin!"

        binding = MainFragmentBinding.inflate(inflater,container,false)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


        with (binding.recyclerView) {
            setHasFixedSize(true) // all rows would have a fix size regardless of its contents

            // to create a divider to separate each row
            val divider = DividerItemDecoration (context, LinearLayoutManager(context).orientation)
            addItemDecoration(divider)
        }

        // this is triggered when notesList data changes
        // noteList is define in the MainViewModel
        viewModel.notesList?.observe(viewLifecycleOwner, Observer {
            // send the reference of this@MainFragment, which contains the
            // override functions for ListItemListener
            adapter = NotesListAdapter(
                it,
                this@MainFragment
            )
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(activity)

            // restore when the fragment comes back from state/orientation change
            val selectedNotes =
                savedInstanceState?.getParcelableArrayList<NoteEntity>(
                    SELECTED_NOTES_KEY
                )
            adapter.selectedNotes.addAll(selectedNotes ?: emptyList())

        })

        // floatingActionButton refers to the add icon on the lower right
        // see main_fragment.xml

        //ALLOWS USER TO ADD A NEW NOTE
        binding.addNewNoteFloatingActionButton.setOnClickListener {
            editNote(NEW_NOTE_ID)
        }

        //ALLOWS USERS TO DELETE ALL NOTES
        binding.deleteAllFloatingActionButton.setOnClickListener{

            //A Builder was created to promt the user for confirmation
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("DELETE EVERYTHING!")
            builder.setMessage("Are you sure you want to do this?")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                deleteAllNotes()
            }
            builder.show()

            //deleteAllNotes()

        }

        //User can select one note and edit it
        binding.editFloatingActionButton.setOnClickListener {
            try {
                if (adapter.selectedNotes.size > 1) {
                    Toast.makeText(
                        context,
                        "You can only do one thing at a time! Select one!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (adapter.selectedNotes.size == 1) {
                    val action = MainFragmentDirections.actionEditNote(adapter.selectedNotes[0].id)
                    findNavController().navigate(action)
                } else if (adapter.selectedNotes.size < 1){
                    Toast.makeText(context,"You have to select something!",Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception){
                Toast.makeText(
                    context,
                    "Let's try that again.",Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root   // now, have access to all the elements in the main_fragment.xml


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuId =
            if (this::adapter.isInitialized &&
                adapter.selectedNotes.isNotEmpty()
            ) {
                R.menu.menu_main_selected_items
            } else {
                R.menu.menu_main
            }
        inflater.inflate(menuId, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sample_data -> addSampleData()
            R.id.action_delete -> deleteSelectedNotes()
            R.id.deleteAllId -> deleteAllNotes()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAllNotes(): Boolean {
        viewModel.deleteAllNotes()
        return true
    }

    private fun deleteSelectedNotes(): Boolean {
        // this deletes from the database
        viewModel.deleteNotes(adapter.selectedNotes)

        // this clears the notes selected, Handler - https://developer.android.com/reference/kotlin/android/os/Handler
        Handler(Looper.getMainLooper()).postDelayed({
            adapter.selectedNotes.clear()
            requireActivity().invalidateOptionsMenu()
        }, 100)
        return true
    }

    private fun addSampleData(): Boolean {
        viewModel.addSampleData()  // implemented in MainViewModel
        return true
    }

    // automatically gets called when the state is about to change
    // e.g. change of orientation from portrait to landscape
    // Will save the entire array
    override fun onSaveInstanceState(outState: Bundle) {
        if (this::adapter.isInitialized) {
            outState.putParcelableArrayList(
                SELECTED_NOTES_KEY,
                adapter.selectedNotes
            )
        }
        super.onSaveInstanceState(outState)
    }
    // from ListItemListener from NotesListAdapter
    override fun editNote(noteId: Int) {
        Log.i("editNote", "noteId = $noteId, called by floatingActionButton")
        // actionEditNote (go to nav_graph.xml and see the code)
        val action =
            MainFragmentDirections.actionEditNote(noteId)
        findNavController().navigate(action)  // findNavController

        // the update is not yet implemented
    }

    override fun onItemSelectionChanged() {
        // informs Android that the menu needs to be redrawn.
        // calls onCreateOptionsMenu
        requireActivity().invalidateOptionsMenu()
    }




}