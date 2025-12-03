package com.domicoder.miunieventos.data.remote

import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
        private const val CATEGORIES_COLLECTION = "categories"
        private const val DEPARTMENTS_COLLECTION = "departments"
    }

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection(CATEGORIES_COLLECTION)
            .whereEqualTo("active", true)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    fun getDepartments(): Flow<List<Department>> = callbackFlow {
        val listener = firestore.collection(DEPARTMENTS_COLLECTION)
            .whereEqualTo("active", true)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val departments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Department::class.java)
                } ?: emptyList()
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
            }
            Result.success(Unit)
        } catch (e: Exception) {
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
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

