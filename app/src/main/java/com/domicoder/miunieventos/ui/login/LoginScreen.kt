package com.domicoder.miunieventos.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.data.repository.AuthResult

@Composable
fun LoginScreen(
    onLoginSuccess: (String, Boolean) -> Unit,
    onRegisterRequest: (() -> Unit)? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val activity = context as? Activity
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authResult by viewModel.authResult.collectAsState()
    val googleSignInIntent by viewModel.googleSignInIntent.collectAsState()
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("LoginScreen", "Google Sign-In result received, resultCode: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            android.util.Log.d("LoginScreen", "Result OK, processing Google Sign-In result")
            viewModel.handleGoogleSignInResult(result.data)
        } else {
            android.util.Log.w("LoginScreen", "Result not OK, user may have cancelled")
            viewModel.setError("El inicio de sesión con Google fue cancelado")
        }
    }
    
    LaunchedEffect(googleSignInIntent) {
        googleSignInIntent?.let { intent ->
            android.util.Log.d("LoginScreen", "Launching Google Sign-In intent")
            googleSignInLauncher.launch(intent)
            viewModel.clearGoogleSignInIntent()
        }
    }
    
    LaunchedEffect(authResult) {
        authResult?.let { result ->
            android.util.Log.d("LoginScreen", "authResult changed: ${result.javaClass.simpleName}")
            when (result) {
                is AuthResult.Success -> {
                    android.util.Log.d("LoginScreen", "AuthResult.Success detected, user ID: ${result.user.id}")
                    viewModel.clearAuthResult()
                    android.util.Log.d("LoginScreen", "Calling onLoginSuccess")
                    onLoginSuccess(result.user.id, rememberMe)
                    android.util.Log.d("LoginScreen", "onLoginSuccess called")
                }
                is AuthResult.Error -> {
                    android.util.Log.e("LoginScreen", "AuthResult.Error: ${result.message}")
                }
            }
        }
    }
    
    fun handleLogin() {
        if (email.isNotBlank() && password.isNotBlank()) {
            viewModel.login(email, password, rememberMe)
        }
    }
    
    fun handleGoogleSignIn() {
        android.util.Log.d("LoginScreen", "handleGoogleSignIn called")
        if (activity == null) {
            android.util.Log.e("LoginScreen", "Activity is null!")
            viewModel.setError("No se pudo iniciar Google Sign-In: Activity es null")
            return
        }
        android.util.Log.d("LoginScreen", "Activity found, starting Google Sign-In")
        viewModel.startGoogleSignIn(activity!!)
    }
    
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "¡Bienvenido de nuevo, te extrañamos!",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Input Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.outline,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface
                ),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
            
            // Forgot Password Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign In Button
        Button(
            onClick = { handleLogin() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Iniciar sesión",
                    color = colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Create New Account
        Button(
            onClick = { onRegisterRequest?.invoke() },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp)
                .border(1.dp, colorScheme.primary, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.background
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Crear cuenta",
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "O continúa con",
            color = colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Social Login Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val googleIcon = ImageVector.vectorResource(id = R.drawable.google_logo)
            val microsoftIcon = ImageVector.vectorResource(id = R.drawable.microsoft_logo)

            // Google Button
            SocialLoginButton(
                text = "G",
                imageVector = googleIcon,
                onClick = {
                    handleGoogleSignIn()
                },
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Microsoft Button
            SocialLoginButton(
                text = "M",
                imageVector = microsoftIcon,
                onClick = {
                    viewModel.signInWithMicrosoft()
                },
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    imageVector: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageVector != null) {
            Image(
                imageVector = imageVector,
                contentDescription = "Login with $text",
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
