package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.domicoder.miunieventos.data.remote.ConfigRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val remoteDataSource: ConfigRemoteDataSource
) {
    fun getCategories(): Flow<List<Category>> = remoteDataSource.getCategories()
    
    fun getDepartments(): Flow<List<Department>> = remoteDataSource.getDepartments()
    
    suspend fun initializeCategories(categories: List<Category>) = 
        remoteDataSource.initializeCategories(categories)
    
    suspend fun initializeDepartments(departments: List<Department>) = 
        remoteDataSource.initializeDepartments(departments)
}

