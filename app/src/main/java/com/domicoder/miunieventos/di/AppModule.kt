package com.domicoder.miunieventos.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // This module is now empty as all dependencies are provided by DataModule
} 