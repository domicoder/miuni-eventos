package com.domicoder.miunieventos.di

import android.content.Context
import com.domicoder.miunieventos.data.remote.AttendanceRemoteDataSource
import com.domicoder.miunieventos.data.remote.AuthRemoteDataSource
import com.domicoder.miunieventos.data.remote.ConfigRemoteDataSource
import com.domicoder.miunieventos.data.remote.EventRemoteDataSource
import com.domicoder.miunieventos.data.remote.RSVPRemoteDataSource
import com.domicoder.miunieventos.data.remote.UserRemoteDataSource
import com.domicoder.miunieventos.data.repository.AttendanceRepository
import com.domicoder.miunieventos.data.repository.AuthRepository as OldAuthRepository
import com.domicoder.miunieventos.data.repository.AuthRepositoryImpl
import com.domicoder.miunieventos.data.repository.ConfigRepository
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.data.repository.UserRepository
import com.domicoder.miunieventos.data.repository.FirestoreInitializer
import com.domicoder.miunieventos.domain.repository.AuthRepository
import com.domicoder.miunieventos.util.AuthPersistenceManager
import com.domicoder.miunieventos.util.LoginStateManager
import com.domicoder.miunieventos.util.UserStateManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.domicoder.miunieventos.data.remote.ImageStorageDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    // Firebase providers
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    // Remote Data Sources
    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRemoteDataSource {
        return AuthRemoteDataSource(firebaseAuth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideEventRemoteDataSource(
        firestore: FirebaseFirestore
    ): EventRemoteDataSource {
        return EventRemoteDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
        firestore: FirebaseFirestore
    ): UserRemoteDataSource {
        return UserRemoteDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideRSVPRemoteDataSource(
        firestore: FirebaseFirestore
    ): RSVPRemoteDataSource {
        return RSVPRemoteDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideAttendanceRemoteDataSource(
        firestore: FirebaseFirestore
    ): AttendanceRemoteDataSource {
        return AttendanceRemoteDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideImageStorageDataSource(
        storage: FirebaseStorage
    ): ImageStorageDataSource {
        return ImageStorageDataSource(storage)
    }
    
    @Provides
    @Singleton
    fun provideConfigRemoteDataSource(
        firestore: FirebaseFirestore
    ): ConfigRemoteDataSource {
        return ConfigRemoteDataSource(firestore)
    }
    
    // Repositories
    @Provides
    @Singleton
    fun provideEventRepository(
        remoteDataSource: EventRemoteDataSource
    ): EventRepository {
        return EventRepository(remoteDataSource)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        remoteDataSource: UserRemoteDataSource
    ): UserRepository {
        return UserRepository(remoteDataSource)
    }
    
    @Provides
    @Singleton
    fun provideRSVPRepository(
        remoteDataSource: RSVPRemoteDataSource
    ): RSVPRepository {
        return RSVPRepository(remoteDataSource)
    }
    
    @Provides
    @Singleton
    fun provideAttendanceRepository(
        remoteDataSource: AttendanceRemoteDataSource
    ): AttendanceRepository {
        return AttendanceRepository(remoteDataSource)
    }
    
    @Provides
    @Singleton
    fun provideConfigRepository(
        remoteDataSource: ConfigRemoteDataSource
    ): ConfigRepository {
        return ConfigRepository(remoteDataSource)
    }
    
    // Domain Repository Implementation (new architecture)
    @Provides
    @Singleton
    fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository {
        return authRepositoryImpl
    }
    
    // Legacy AuthRepository (for backward compatibility)
    @Provides
    @Singleton
    fun provideOldAuthRepository(
        userRemoteDataSource: UserRemoteDataSource
    ): OldAuthRepository {
        return OldAuthRepository(userRemoteDataSource)
    }
    
    @Provides
    @Singleton
    fun provideFirestoreInitializer(
        eventRemoteDataSource: EventRemoteDataSource,
        userRemoteDataSource: UserRemoteDataSource,
        rsvpRemoteDataSource: RSVPRemoteDataSource,
        attendanceRemoteDataSource: AttendanceRemoteDataSource,
        configRemoteDataSource: ConfigRemoteDataSource
    ): FirestoreInitializer {
        return FirestoreInitializer(
            eventRemoteDataSource,
            userRemoteDataSource,
            rsvpRemoteDataSource,
            attendanceRemoteDataSource,
            configRemoteDataSource
        )
    }
    
    @Provides
    @Singleton
    fun provideAuthPersistenceManager(@ApplicationContext context: Context): AuthPersistenceManager {
        return AuthPersistenceManager(context)
    }
    
    @Provides
    @Singleton
    fun provideUserStateManager(
        userRepository: UserRepository,
        authPersistenceManager: AuthPersistenceManager,
        authRepository: AuthRepository
    ): UserStateManager {
        return UserStateManager(userRepository, authPersistenceManager, authRepository)
    }
    
    @Provides
    @Singleton
    fun provideLoginStateManager(
        authRepository: OldAuthRepository,
        userStateManager: UserStateManager,
        authPersistenceManager: AuthPersistenceManager
    ): LoginStateManager {
        return LoginStateManager(authRepository, userStateManager, authPersistenceManager)
    }
}
