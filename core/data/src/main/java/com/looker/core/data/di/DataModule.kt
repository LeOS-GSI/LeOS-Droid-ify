package com.leos.core.data.di

import com.leos.core.data.fdroid.repository.AppRepository
import com.leos.core.data.fdroid.repository.RepoRepository
import com.leos.core.data.fdroid.repository.offline.OfflineFirstAppRepository
import com.leos.core.data.fdroid.repository.offline.OfflineFirstRepoRepository
import com.leos.core.data.fdroid.sync.IndexDownloader
import com.leos.core.data.fdroid.sync.IndexDownloaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindsAppRepository(
        appRepository: OfflineFirstAppRepository
    ): AppRepository

    @Binds
    fun bindsRepoRepository(
        repoRepository: OfflineFirstRepoRepository
    ): RepoRepository

    @Binds
    fun bindIndexDownloader(
        indexDownloader: IndexDownloaderImpl
    ): IndexDownloader
}
