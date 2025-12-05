package com.orca.tracker.di


import android.content.Context
import androidx.room.Room
import com.orca.tracker.data.database.OrcaDatabase
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
    fun provideOrcaDatabase(@ApplicationContext context: Context): OrcaDatabase {
        return Room.databaseBuilder(
            context,
            OrcaDatabase::class.java,
            "orca_database"
        ).build()
    }

    @Provides
    fun provideRunDao(database: OrcaDatabase) = database.runDao()

    @Provides
    fun provideLocationPointDao(database: OrcaDatabase) = database.locationPointDao()

    @Provides
    fun provideSettingsDao(database: OrcaDatabase) = database.settingsDao()
}