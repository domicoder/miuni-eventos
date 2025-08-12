package com.domicoder.miunieventos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
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