package com.domicoder.miunieventos.di

import android.content.Context
import com.domicoder.miunieventos.data.local.AppDatabase
import com.domicoder.miunieventos.data.local.EventDao
import com.domicoder.miunieventos.data.local.RSVPDao
import com.domicoder.miunieventos.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideEventDao(appDatabase: AppDatabase): EventDao {
        return appDatabase.eventDao()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
    
    @Provides
    @Singleton
    fun provideRSVPDao(appDatabase: AppDatabase): RSVPDao {
        return appDatabase.rsvpDao()
    }
} 