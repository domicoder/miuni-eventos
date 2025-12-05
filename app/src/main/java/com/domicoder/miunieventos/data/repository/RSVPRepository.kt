package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.remote.RSVPRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RSVPRepository @Inject constructor(
    private val remoteDataSource: RSVPRemoteDataSource
) {
    open fun getAllRSVPs(): Flow<List<RSVP>> = remoteDataSource.getAllRSVPs()
    
    open fun getRSVPsByEventId(eventId: String): Flow<List<RSVP>> = 
        remoteDataSource.getRSVPsByEventId(eventId)
    
    open fun getRSVPsByUserId(userId: String): Flow<List<RSVP>> = 
        remoteDataSource.getRSVPsByUserId(userId)
    
    open fun getRSVPsByUserAndStatus(userId: String, status: RSVPStatus): Flow<List<RSVP>> =
        remoteDataSource.getRSVPsByUserAndStatus(userId, status)
    
    open suspend fun getRSVPById(id: String): RSVP? = remoteDataSource.getRSVPById(id)
    
    open suspend fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP? = 
        remoteDataSource.getRSVPByEventAndUser(eventId, userId)
    
    open suspend fun countRSVPsByEventAndStatus(eventId: String, status: RSVPStatus): Int =
        remoteDataSource.countRSVPsByEventAndStatus(eventId, status)
    
    open suspend fun insertRSVP(rsvp: RSVP): Result<String> = remoteDataSource.insertRSVP(rsvp)
    
    open suspend fun updateRSVP(rsvp: RSVP): Result<Unit> = remoteDataSource.updateRSVP(rsvp)
    
    open suspend fun upsertRSVP(rsvp: RSVP): Result<Unit> = remoteDataSource.upsertRSVP(rsvp)
    
    open suspend fun deleteRSVP(rsvp: RSVP): Result<Unit> = remoteDataSource.deleteRSVP(rsvp)
    
    open suspend fun deleteRSVPByEventAndUser(eventId: String, userId: String): Result<Unit> = 
        remoteDataSource.deleteRSVPByEventAndUser(eventId, userId)
}
