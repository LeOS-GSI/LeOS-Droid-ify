package com.leos.core.data.di

import com.leos.core.data.fdroid.sync.IndexDownloader
import com.leos.core.data.fdroid.sync.IndexManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fdroid.index.IndexConverter

@Module
@InstallIn(SingletonComponent::class)
object DataModuleSingleton {

    @Provides
    fun provideIndexManager(
        downloader: IndexDownloader
    ): IndexManager = IndexManager(
        indexDownloader = downloader,
        converter = IndexConverter()
    )
}
