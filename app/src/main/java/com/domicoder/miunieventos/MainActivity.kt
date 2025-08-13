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
import androidx.lifecycle.lifecycleScope
import com.domicoder.miunieventos.ui.navigation.AppNavigation
import com.domicoder.miunieventos.ui.theme.MiUNIEventosTheme
import com.domicoder.miunieventos.util.DeepLinkManager
import com.domicoder.miunieventos.util.UserStateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userStateManager: UserStateManager
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle deep links when app is opened
        handleDeepLink(intent)
        
        // Restore authentication state from persistent storage
        restoreAuthenticationState()
        
        setContent {
            MiUNIEventosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(userStateManager = userStateManager)
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
    
    /**
     * Restores authentication state from persistent storage on app startup
     * This ensures users don't need to login again if the app was closed unexpectedly
     */
    private fun restoreAuthenticationState() {
        lifecycleScope.launch {
            try {
                val restored = userStateManager.restoreAuthenticationState()
                if (restored) {
                    Log.d(TAG, "Authentication state restored successfully")
                } else {
                    Log.d(TAG, "No valid authentication state found, user needs to login")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring authentication state", e)
            }
        }
    }
}