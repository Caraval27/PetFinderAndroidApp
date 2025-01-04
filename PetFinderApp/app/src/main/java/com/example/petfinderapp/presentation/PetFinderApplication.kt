package com.example.petfinderapp.presentation

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database

class PetFinderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
    }
}