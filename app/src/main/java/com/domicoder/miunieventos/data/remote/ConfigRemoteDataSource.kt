package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "ConfigRemoteDataSource"
        private const val CATEGORIES_COLLECTION = "categories"
        private const val DEPARTMENTS_COLLECTION = "departments"
    }

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection(CATEGORIES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting categories", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)
                }?.filter { it.active }
                    ?.sortedBy { it.order }
                    ?: emptyList()
                
                Log.d(TAG, "Categories loaded: ${categories.size}")
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    fun getDepartments(): Flow<List<Department>> = callbackFlow {
        val listener = firestore.collection(DEPARTMENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting departments", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val departments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Department::class.java)
                }?.filter { it.active }
                    ?.sortedBy { it.order }
                    ?: emptyList()
                
                Log.d(TAG, "Departments loaded: ${departments.size}")
                trySend(departments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun initializeCategories(categories: List<Category>): Result<Unit> {
        return try {
            val existing = firestore.collection(CATEGORIES_COLLECTION).get().await()
            if (existing.isEmpty) {
                val batch = firestore.batch()
                categories.forEach { category ->
                    val docRef = firestore.collection(CATEGORIES_COLLECTION).document()
                    batch.set(docRef, category.copy(id = docRef.id))
                }
                batch.commit().await()
                Log.d(TAG, "Categories initialized: ${categories.size}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing categories", e)
            Result.failure(e)
        }
    }

    suspend fun initializeDepartments(departments: List<Department>): Result<Unit> {
        return try {
            val existing = firestore.collection(DEPARTMENTS_COLLECTION).get().await()
            if (existing.isEmpty) {
                val batch = firestore.batch()
                departments.forEach { department ->
                    val docRef = firestore.collection(DEPARTMENTS_COLLECTION).document()
                    batch.set(docRef, department.copy(id = docRef.id))
                }
                batch.commit().await()
                Log.d(TAG, "Departments initialized: ${departments.size}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing departments", e)
            Result.failure(e)
        }
    }
}
