package com.domicoder.miunieventos.util

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "GoogleSignInHelper"
    }
    
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val webClientId = getDefaultWebClientId(activity)
        
        if (webClientId.isEmpty()) {
            throw IllegalStateException("No se pudo obtener el client_id de Google. Verifica que google-services.json esté configurado correctamente.")
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(activity, gso)
    }
    
    private fun getDefaultWebClientId(activity: Activity): String {
        try {
            val resources = activity.resources
            val packageName = activity.packageName
            
            var resourceId = resources.getIdentifier(
                "default_web_client_id",
                "string",
                packageName
            )
            
            if (resourceId == 0) {
                try {
                    val rClass = Class.forName("$packageName.R\$string")
                    val field = rClass.getField("default_web_client_id")
                    resourceId = field.get(null) as Int
                } catch (e: Exception) {
                }
            }
            
            if (resourceId != 0) {
                val clientId = resources.getString(resourceId)
                if (clientId.isNotEmpty()) {
                    return clientId
                }
            }
        } catch (e: Exception) {
        }
        
        try {
            return readClientIdFromAssets(activity)
        } catch (e: Exception) {
            throw IllegalStateException(
                "No se pudo obtener el client_id de Google.\n\n" +
                "SOLUCIÓN:\n" +
                "1. Ve a Firebase Console (https://console.firebase.google.com/)\n" +
                "2. Selecciona tu proyecto\n" +
                "3. Ve a Authentication > Sign-in method\n" +
                "4. Habilita 'Google' como proveedor de autenticación\n" +
                "5. Configura el nombre del proyecto y el email de soporte\n" +
                "6. Guarda los cambios\n" +
                "7. Descarga el nuevo google-services.json desde Project Settings\n" +
                "8. Reemplaza el archivo en app/google-services.json\n" +
                "9. Recompila el proyecto\n\n" +
                "Error técnico: ${e.message}"
            )
        }
    }
    
    private fun readClientIdFromAssets(activity: Activity): String {
        val inputStream = activity.applicationContext.assets.open("google-services.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = org.json.JSONObject(json)
        
        try {
            val clients = jsonObject.getJSONArray("client")
            
            for (i in 0 until clients.length()) {
                val client = clients.getJSONObject(i)
                
                if (client.has("oauth_client")) {
                    val oauthClients = client.getJSONArray("oauth_client")
                    
                    if (oauthClients.length() == 0) {
                        continue
                    }
                    
                    for (j in 0 until oauthClients.length()) {
                        val oauthClient = oauthClients.getJSONObject(j)
                        if (oauthClient.optString("client_type") == "3") {
                            return oauthClient.getString("client_id")
                        }
                    }
                    
                    if (oauthClients.length() > 0) {
                        return oauthClients.getJSONObject(0).getString("client_id")
                    }
                }
            }
        } catch (e: Exception) {
            try {
                val client = jsonObject.getJSONObject("client")
                if (client.has("oauth_client")) {
                    val oauthClients = client.getJSONArray("oauth_client")
                    if (oauthClients.length() > 0) {
                        for (j in 0 until oauthClients.length()) {
                            val oauthClient = oauthClients.getJSONObject(j)
                            if (oauthClient.optString("client_type") == "3") {
                                return oauthClient.getString("client_id")
                            }
                        }
                        return oauthClients.getJSONObject(0).getString("client_id")
                    }
                }
            } catch (e2: Exception) {
            }
        }
        
        throw Exception(
            "No se encontró ningún cliente OAuth web en google-services.json.\n\n" +
            "SOLUCIÓN:\n" +
            "1. Ve a Firebase Console (https://console.firebase.google.com/)\n" +
            "2. Selecciona tu proyecto\n" +
            "3. Ve a Authentication > Sign-in method\n" +
            "4. Habilita 'Google' como proveedor de autenticación\n" +
            "5. Configura el nombre del proyecto y el email de soporte\n" +
            "6. Guarda los cambios\n" +
            "7. Descarga el nuevo google-services.json desde Project Settings > General\n" +
            "8. Reemplaza el archivo en app/google-services.json\n" +
            "9. Recompila el proyecto (Build > Rebuild Project)"
        )
    }
    
    fun getSignInIntent(activity: Activity): Intent {
        Log.d(TAG, "getSignInIntent called")
        val googleSignInClient = getGoogleSignInClient(activity)
        googleSignInClient.signOut()
        val intent = googleSignInClient.signInIntent
        Log.d(TAG, "Google sign in intent created successfully")
        return intent
    }
    
    fun handleSignInResult(data: Intent?): Result<com.google.firebase.auth.AuthCredential> {
        Log.d(TAG, "handleSignInResult called, data is null: ${data == null}")
        return try {
            if (data == null) {
                Log.e(TAG, "Intent data is null")
                return Result.failure(Exception("No se recibió ningún dato del resultado de Google Sign-In"))
            }
            
            Log.d(TAG, "Getting signed in account from intent")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d(TAG, "Task received, getting result")
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Account received: ${account?.email}")
            
            account?.let {
                if (it.idToken == null) {
                    Log.e(TAG, "Account idToken is null")
                    return Result.failure(Exception("No se pudo obtener el token de Google"))
                }
                Log.d(TAG, "Account idToken length: ${it.idToken?.length}")
                Log.d(TAG, "Account email: ${it.email}, id: ${it.id}")
                Log.d(TAG, "Creating credential from idToken")
                val credential = GoogleAuthProvider.getCredential(it.idToken, null)
                Log.d(TAG, "Credential created successfully, provider: ${credential.provider}")
                Result.success(credential)
            } ?: run {
                Log.e(TAG, "Account is null")
                Result.failure(Exception("No se pudo obtener la cuenta de Google"))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "ApiException in handleSignInResult: ${e.message}", e)
            Result.failure(Exception("Error al iniciar sesión con Google: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception in handleSignInResult: ${e.message}", e)
            Result.failure(e)
        }
    }
}

