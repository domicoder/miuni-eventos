package com.domicoder.miunieventos.data.model

import com.google.firebase.firestore.DocumentId

data class Location(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val building: String? = null,
    val room: String? = null,
    val campus: String? = null
) {
    fun getFullAddress(): String {
        return buildString {
            append(address)
            building?.let { append(", $it") }
            room?.let { append(" - Room $it") }
        }
    }
    
    fun getDisplayName(): String {
        return buildString {
            append(name)
            building?.let { append(" ($it)") }
        }
    }
}
