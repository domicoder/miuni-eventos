package com.domicoder.miunieventos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RSVPDao {
    @Query("SELECT * FROM rsvps")
    fun getAllRSVPs(): Flow<List<RSVP>>
    
    @Query("SELECT * FROM rsvps WHERE id = :id")
    suspend fun getRSVPById(id: Long): RSVP?
    
    @Query("SELECT * FROM rsvps WHERE eventId = :eventId")
    fun getRSVPsByEventId(eventId: String): Flow<List<RSVP>>
    
    @Query("SELECT * FROM rsvps WHERE userId = :userId")
    fun getRSVPsByUserId(userId: String): Flow<List<RSVP>>
    
    @Query("SELECT * FROM rsvps WHERE eventId = :eventId AND userId = :userId")
    suspend fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP?
    
    @Query("SELECT * FROM rsvps WHERE userId = :userId AND status = :status")
    fun getRSVPsByUserAndStatus(userId: String, status: RSVPStatus): Flow<List<RSVP>>
    
    @Query("SELECT COUNT(*) FROM rsvps WHERE eventId = :eventId AND status = :status")
    suspend fun countRSVPsByEventAndStatus(eventId: String, status: RSVPStatus): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRSVP(rsvp: RSVP): Long
    
    @Update
    suspend fun updateRSVP(rsvp: RSVP)
    
    @Delete
    suspend fun deleteRSVP(rsvp: RSVP)
    
    @Query("DELETE FROM rsvps WHERE eventId = :eventId AND userId = :userId")
    suspend fun deleteRSVPByEventAndUser(eventId: String, userId: String)
    
    @Transaction
    suspend fun upsertRSVP(rsvp: RSVP) {
        val existingRSVP = getRSVPByEventAndUser(rsvp.eventId, rsvp.userId)
        if (existingRSVP == null) {
            insertRSVP(rsvp)
        } else {
            updateRSVP(rsvp.copy(id = existingRSVP.id))
        }
    }
} 