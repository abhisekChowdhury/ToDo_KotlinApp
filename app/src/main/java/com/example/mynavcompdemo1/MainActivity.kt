package com.example.mynavcompdemo1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mynavcompdemo1.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            Step 1

            1. MainActivity loads activity_main layout
            2. activity_main.xml contains app:navGraph="@navigation/nav_graph"
               which "loads" the contents of nav_graph.xml
            3. Inside nav_graph.xml, it first "execute" mainFragment which contains
               a RecyclerView (android:id="@+id/recyclerView") and a floatingActionButton (android:id="@+id/floatingActionButton")

            Go to MainFragment.kt

         */
    }
}