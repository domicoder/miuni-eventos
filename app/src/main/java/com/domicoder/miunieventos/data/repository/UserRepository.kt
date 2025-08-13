package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.local.UserDao
import com.domicoder.miunieventos.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    open fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    
    open fun getOrganizers(): Flow<List<User>> = userDao.getOrganizers()
    
    open suspend fun getUserById(id: String): User? = userDao.getUserById(id)
    
    open suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    
    open suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    open suspend fun insertUsers(users: List<User>) = userDao.insertUsers(users)
    
    open suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    open suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    
    open suspend fun deleteUserById(id: String) = userDao.deleteUserById(id)
} 