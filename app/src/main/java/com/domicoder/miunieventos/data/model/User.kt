package com.domicoder.miunieventos.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val department: String? = null,
    val isOrganizer: Boolean = false
)
