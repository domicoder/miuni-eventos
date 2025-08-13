package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.local.RSVPDao
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RSVPRepository @Inject constructor(
    private val rsvpDao: RSVPDao
) {
    open fun getAllRSVPs(): Flow<List<RSVP>> = rsvpDao.getAllRSVPs()
    
    open fun getRSVPsByEventId(eventId: String): Flow<List<RSVP>> = 
        rsvpDao.getRSVPsByEventId(eventId)
    
    open fun getRSVPsByUserId(userId: String): Flow<List<RSVP>> = 
        rsvpDao.getRSVPsByUserId(userId)
    
    open fun getRSVPsByUserAndStatus(userId: String, status: RSVPStatus): Flow<List<RSVP>> =
        rsvpDao.getRSVPsByUserAndStatus(userId, status)
    
    open suspend fun getRSVPById(id: Long): RSVP? = rsvpDao.getRSVPById(id)
    
    open suspend fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP? = 
        rsvpDao.getRSVPByEventAndUser(eventId, userId)
    
    open suspend fun countRSVPsByEventAndStatus(eventId: String, status: RSVPStatus): Int =
        rsvpDao.countRSVPsByEventAndStatus(eventId, status)
    
    open suspend fun insertRSVP(rsvp: RSVP): Long = rsvpDao.insertRSVP(rsvp)
    
    open suspend fun updateRSVP(rsvp: RSVP) = rsvpDao.updateRSVP(rsvp)
    
    open suspend fun upsertRSVP(rsvp: RSVP) = rsvpDao.upsertRSVP(rsvp)
    
    open suspend fun deleteRSVP(rsvp: RSVP) = rsvpDao.deleteRSVP(rsvp)
    
    open suspend fun deleteRSVPByEventAndUser(eventId: String, userId: String) = 
        rsvpDao.deleteRSVPByEventAndUser(eventId, userId)
} 