package com.leos.core.datastore

import android.net.Uri
import com.leos.core.datastore.model.AutoSync
import com.leos.core.datastore.model.InstallerType
import com.leos.core.datastore.model.ProxyType
import com.leos.core.datastore.model.SortOrder
import com.leos.core.datastore.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

interface SettingsRepository {

    val data: Flow<Settings>

    suspend fun getInitial(): Settings

    suspend fun export(target: Uri)

    suspend fun import(target: Uri)

    suspend fun setLanguage(language: String)

    suspend fun enableIncompatibleVersion(enable: Boolean)

    suspend fun enableNotifyUpdates(enable: Boolean)

    suspend fun enableUnstableUpdates(enable: Boolean)

    suspend fun setTheme(theme: Theme)

    suspend fun setDynamicTheme(enable: Boolean)

    suspend fun setInstallerType(installerType: InstallerType)

    suspend fun setAutoUpdate(allow: Boolean)

    suspend fun setAutoSync(autoSync: AutoSync)

    suspend fun setSortOrder(sortOrder: SortOrder)

    suspend fun setProxyType(proxyType: ProxyType)

    suspend fun setProxyHost(proxyHost: String)

    suspend fun setProxyPort(proxyPort: Int)

    suspend fun setCleanUpInterval(interval: Duration)

    suspend fun setCleanupInstant()

    suspend fun setHomeScreenSwiping(value: Boolean)

    suspend fun toggleFavourites(packageName: String)
}

inline fun <T> SettingsRepository.get(crossinline block: suspend Settings.() -> T): Flow<T> {
    return data.map(block).distinctUntilChanged()
}
