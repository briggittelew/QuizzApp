package com.briggittelew.quizzapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.briggittelew.quizzapp.databinding.ActivityInitBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class InitActivity : AppCompatActivity() {
    private val marksCollection: CollectionReference
    private lateinit var b: ActivityInitBinding

    init{
        FirebaseApp.initializeApp(this)
        marksCollection = FirebaseFirestore.getInstance().collection("marks")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityInitBinding.inflate(layoutInflater)
        setContentView(b.root)

        fun addToList(mark: Mark) {
            var name = b.tvName.text.toString()
            name += mark.toString() + "\n"
            b.tvName.text = name
        }
    }
}