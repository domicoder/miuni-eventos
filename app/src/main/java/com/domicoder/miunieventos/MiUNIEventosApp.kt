package com.domicoder.miunieventos

import android.app.Application
import com.domicoder.miunieventos.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MiUNIEventosApp : Application() {
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer
    
    override fun onCreate() {
        super.onCreate()
        // Initialize database with initial data
        CoroutineScope(Dispatchers.IO).launch {
            databaseInitializer.initializeDatabase()
        }
    }
} 