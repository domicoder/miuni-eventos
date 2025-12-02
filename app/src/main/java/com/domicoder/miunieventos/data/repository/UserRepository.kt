package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserRepository @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource
) {
    open fun getAllUsers(): Flow<List<User>> = remoteDataSource.getAllUsers()
    
    open fun getOrganizers(): Flow<List<User>> = remoteDataSource.getOrganizers()
    
    open suspend fun getUserById(id: String): User? = remoteDataSource.getUserById(id)
    
    open suspend fun getUserByEmail(email: String): User? = remoteDataSource.getUserByEmail(email)
    
    open suspend fun insertUser(user: User): Result<String> = remoteDataSource.insertUser(user)
    
    open suspend fun insertUsers(users: List<User>): Result<Unit> = remoteDataSource.insertUsers(users)
    
    open suspend fun updateUser(user: User): Result<Unit> = remoteDataSource.updateUser(user)
    
    open suspend fun deleteUser(user: User): Result<Unit> = remoteDataSource.deleteUser(user)
    
    open suspend fun deleteUserById(id: String): Result<Unit> = remoteDataSource.deleteUserById(id)
}
