package com.example.mynavcompdemo1

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynavcompdemo1.databinding.EditorFragmentBinding
import com.example.mynavcompdemo1.EditorFragmentArgs
import com.example.mynavcompdemo1.R

/*
    Step 3
    1. Edit an existing note or add a new one
 */
class EditorFragment : Fragment() {

    private lateinit var viewModel: EditorViewModel

    // navArgs property delegate to access arguments
    private val args: EditorFragmentArgs by navArgs()
    private lateinit var binding: EditorFragmentBinding // class name + Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // to reference the ActionBar
        (activity as AppCompatActivity).supportActionBar?.let {
            it.setHomeButtonEnabled(true)        // makes it clickable
            it.setDisplayShowHomeEnabled(true)   // shows <- icon if the R.drawable does not work
            it.setDisplayHomeAsUpEnabled(true)      // shows the icon
            it.setHomeAsUpIndicator(R.drawable.ic_check)
        }
        setHasOptionsMenu(true)  // implement function onOptionsItemSelected (below)

        requireActivity().title =
            /*
                args.noteid is defined inside the nav_graph
                <argument
                    android:name="noteid"
                    app:argType="integer"
                    android:defaultValue="0" />
             */
            if (args.noteid == NEW_NOTE_ID) {
                "New Note"
            } else {
                "Edit Note"
            }

        viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)
        binding = EditorFragmentBinding.inflate(inflater, container, false)
        binding.editor.setText("")   // editor is a field in the editor_fragment.xml

        // this to handle event when the user press the back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveAndReturn()
                }
            }
        )

        // this listens to the changes in the currentNote
        viewModel.currentNote?.observe(viewLifecycleOwner, Observer {
            val savedString = savedInstanceState?.getString(NOTE_TEXT_KEY)
            val cursorPosition = savedInstanceState?.getInt(CURSOR_POSITION_KEY) ?: 0
            binding.editor.setText(savedString ?: it.text)
            // sets the cursor to the last cursor position before the change of state
            binding.editor.setSelection(cursorPosition)
        })

        // This would call getNoteById in the EditorViewModel
        viewModel.getNoteById(args.noteid)  // noteid was defined in nav_graph.xml

        return binding.root
    }

    // ===========================================================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> saveAndReturn()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveAndReturn(): Boolean {
        // to close the soft keyboard
        val imm = requireActivity()
            .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // get the text
        viewModel.currentNote?.value?.text = binding.editor.text.toString()
        viewModel.updateNote()

        // Attempts to navigate up in the navigation hierarchy.
        findNavController().navigateUp()
        return true
    }

    // automatically gets called when the state is about to change
    // e.g. change of orientation from portrait to landscape
    override fun onSaveInstanceState(outState: Bundle) {
        with(binding.editor) {
            outState.putString(NOTE_TEXT_KEY, text.toString())
            // selectionStart is from EditText
            outState.putInt(CURSOR_POSITION_KEY, selectionStart)
        }
        super.onSaveInstanceState(outState)
    }
}