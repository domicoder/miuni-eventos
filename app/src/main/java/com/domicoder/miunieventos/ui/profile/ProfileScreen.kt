package com.domicoder.miunieventos.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.ui.navigation.NavRoutes
import com.domicoder.miunieventos.data.model.UserProfileData

@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String = "",
    isOrganizer: Boolean = false,
    userProfileInfo: UserProfileData = UserProfileData("Usuario", "Departamento"),
    onLogout: () -> Unit
) {
    // Get user information based on userId, but use updated profile info if available
    val userInfo = getUserInfo(userId, userProfileInfo)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Photo
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userInfo.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User Name
        Text(
            text = userInfo.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // User Email
        Text(
            text = userInfo.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // User Department
        Text(
            text = userInfo.department,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // User Role Badge
        Text(
            text = if (isOrganizer) "Organizador" else "Estudiante",
            style = MaterialTheme.typography.labelLarge,
            color = if (isOrganizer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Edit Profile Button
        Button(
            onClick = { 
                navController.navigate(NavRoutes.EditProfile.route)
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
            
            Text(
                text = stringResource(R.string.edit_profile),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isOrganizer) {
            Button(
                onClick = { /* Navigate to organized events */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = stringResource(R.string.my_organized_events))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null
            )
            
            Text(
                text = "Cerrar Sesión",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Text(
            text = "Toca 'Cerrar Sesión' para volver a la pantalla de inicio de sesión",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// Helper function to get user information based on userId
private fun getUserInfo(userId: String, userProfileInfo: UserProfileData): UserInfo {
    val baseInfo = when (userId) {
        "user1" -> UserInfo(
            name = "Juanito Alimaña",
            email = "juanito.alimana@unicda.edu.do",
            department = "Ingeniería Software",
            photoUrl = "https://i.pravatar.cc/300?u=user1"
        )
        "user2" -> UserInfo(
            name = "María González",
            email = "maria.gonzalez@unicda.edu.do",
            department = "Ciencias Sociales",
            photoUrl = "https://i.pravatar.cc/300?u=user2"
        )
        "user3" -> UserInfo(
            name = "Carlos Rodríguez",
            email = "carlos.rodriguez@unicda.edu.do",
            department = "Medicina",
            photoUrl = "https://i.pravatar.cc/300?u=user3"
        )
        else -> UserInfo(
            name = "Usuario",
            email = "usuario@unicda.edu.do",
            department = "Departamento",
            photoUrl = "https://i.pravatar.cc/300?u=default"
        )
    }
    
    // Override with updated profile information if available
    return baseInfo.copy(
        name = userProfileInfo.name,
        department = userProfileInfo.department
    )
}

data class UserInfo(
    val name: String,
    val email: String,
    val department: String,
    val photoUrl: String
) 