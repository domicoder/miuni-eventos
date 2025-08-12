package com.domicoder.miunieventos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.domicoder.miunieventos.ui.navigation.AppNavigation
import com.domicoder.miunieventos.ui.theme.MiUNIEventosTheme
import com.domicoder.miunieventos.util.DeepLinkManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle deep links when app is opened
        handleDeepLink(intent)
        

        
        setContent {
            MiUNIEventosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle deep links when app is already running
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        // Use DeepLinkManager to handle deep links
        DeepLinkManager.handleDeepLink(intent)
    }
    

}