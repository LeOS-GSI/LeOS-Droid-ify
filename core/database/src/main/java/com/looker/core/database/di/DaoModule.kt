package com.leos.core.database.di

import com.leos.core.database.DroidifyDatabase
import com.leos.core.database.dao.AppDao
import com.leos.core.database.dao.InstalledDao
import com.leos.core.database.dao.RepoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    @Singleton
    fun provideRepoDao(
        database: DroidifyDatabase
    ): RepoDao = database.repoDao()

    @Provides
    @Singleton
    fun provideAppDao(
        database: DroidifyDatabase
    ): AppDao = database.appDao()

    @Provides
    @Singleton
    fun provideInstalledDao(
        database: DroidifyDatabase
    ): InstalledDao = database.installedDao()
}
