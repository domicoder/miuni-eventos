package com.domicoder.miunieventos.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val department: String? = null,
    @get:PropertyName("organizer")
    @set:PropertyName("organizer")
    var isOrganizer: Boolean = false
) {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", null, null, false)
}
