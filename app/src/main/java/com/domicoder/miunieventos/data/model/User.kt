package com.domicoder.miunieventos.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val department: String? = null,
    @PropertyName("organizer")
    val organizer: Boolean = false
) {
    constructor() : this("", "", "", null, null, false)

    @get:Exclude
    val isOrganizer: Boolean
        get() = organizer
}
