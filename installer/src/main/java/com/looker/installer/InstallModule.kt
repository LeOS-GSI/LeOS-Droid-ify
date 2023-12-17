package com.leos.installer

import android.content.Context
import com.leos.core.datastore.SettingsRepository
import com.leos.installer.installers.root.RootPermissionHandler
import com.leos.installer.installers.shizuku.ShizukuPermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InstallModule {

    @Singleton
    @Provides
    fun providesInstaller(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): InstallManager = InstallManager(context, settingsRepository)

    @Singleton
    @Provides
    fun provideShizukuPermissionHandler(
        @ApplicationContext context: Context
    ): ShizukuPermissionHandler = ShizukuPermissionHandler(context)

    @Singleton
    @Provides
    fun provideRootPermissionHandler(): RootPermissionHandler = RootPermissionHandler()
}
