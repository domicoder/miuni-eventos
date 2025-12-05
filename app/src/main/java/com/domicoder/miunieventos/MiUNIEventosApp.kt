package com.domicoder.miunieventos

import android.app.Application
import com.domicoder.miunieventos.data.repository.FirestoreInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MiUNIEventosApp : Application() {
    
    @Inject
    lateinit var firestoreInitializer: FirestoreInitializer
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Firestore with initial data
        CoroutineScope(Dispatchers.IO).launch {
            firestoreInitializer.initializeDatabase()
        }
    }
}
