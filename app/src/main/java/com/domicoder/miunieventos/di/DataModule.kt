package com.domicoder.miunieventos.di

import android.content.Context
import com.domicoder.miunieventos.data.local.AppDatabase
import com.domicoder.miunieventos.data.local.DatabaseInitializer
import com.domicoder.miunieventos.data.local.EventDao
import com.domicoder.miunieventos.data.local.RSVPDao
import com.domicoder.miunieventos.data.local.UserDao
import com.domicoder.miunieventos.data.local.AttendanceDao
import com.domicoder.miunieventos.data.repository.AuthRepository
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.data.repository.UserRepository
import com.domicoder.miunieventos.util.LoginStateManager
import com.domicoder.miunieventos.util.UserStateManager
import com.domicoder.miunieventos.data.repository.AttendanceRepository
import com.domicoder.miunieventos.util.AuthPersistenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideRSVPDao(database: AppDatabase): RSVPDao {
        return database.rsvpDao()
    }
    
    @Provides
    @Singleton
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }
    
    @Provides @Singleton fun provideDatabaseInitializer(eventDao: EventDao, userDao: UserDao, rsvpDao: RSVPDao, attendanceDao: AttendanceDao): DatabaseInitializer { return DatabaseInitializer(eventDao, userDao, rsvpDao, attendanceDao) }
    
    @Provides
    @Singleton
    fun provideAuthRepository(userDao: UserDao): AuthRepository {
        return AuthRepository(userDao)
    }
    
    @Provides
    @Singleton
    fun provideAuthPersistenceManager(@ApplicationContext context: Context): AuthPersistenceManager {
        return AuthPersistenceManager(context)
    }
    
    @Provides
    @Singleton
    fun provideUserStateManager(userRepository: UserRepository, authPersistenceManager: AuthPersistenceManager): UserStateManager {
        return UserStateManager(userRepository, authPersistenceManager)
    }
    
    @Provides
    @Singleton
    fun provideLoginStateManager(
        authRepository: AuthRepository,
        userStateManager: UserStateManager,
        authPersistenceManager: AuthPersistenceManager
    ): LoginStateManager {
        return LoginStateManager(authRepository, userStateManager, authPersistenceManager)
    }
    
    @Provides
    @Singleton
    fun provideEventRepository(eventDao: EventDao): EventRepository {
        return EventRepository(eventDao)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }
    
    @Provides
    @Singleton
    fun provideRSVPRepository(rsvpDao: RSVPDao): RSVPRepository {
        return RSVPRepository(rsvpDao)
    }

    @Provides @Singleton fun provideAttendanceRepository(attendanceDao: AttendanceDao): AttendanceRepository { return AttendanceRepository(attendanceDao) }
} 