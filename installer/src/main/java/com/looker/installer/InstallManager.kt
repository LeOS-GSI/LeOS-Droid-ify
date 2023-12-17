package com.leos.installer

import android.content.Context
import com.leos.core.common.Constants
import com.leos.core.common.PackageName
import com.leos.core.common.extension.addAndCompute
import com.leos.core.common.extension.filter
import com.leos.core.common.extension.notificationManager
import com.leos.core.common.extension.updateAsMutable
import com.leos.core.datastore.SettingsRepository
import com.leos.core.datastore.get
import com.leos.core.datastore.model.InstallerType
import com.leos.installer.installers.Installer
import com.leos.installer.installers.LegacyInstaller
import com.leos.installer.installers.root.RootInstaller
import com.leos.installer.installers.session.SessionInstaller
import com.leos.installer.installers.shizuku.ShizukuInstaller
import com.leos.installer.model.InstallItem
import com.leos.installer.model.InstallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// TODO: Fix the stuck state, and other installer
class InstallManager(
    private val context: Context,
    settingsRepository: SettingsRepository
) {

    private val installItems = Channel<InstallItem>()
    private val uninstallItems = Channel<PackageName>()

    val state = MutableStateFlow<Map<PackageName, InstallState>>(emptyMap())

    private var _installer: Installer? = null
        set(value) {
            field?.cleanup()
            field = value
        }
    private val installer: Installer get() = _installer!!

    private val lock = Mutex()
    private val installerPreference = settingsRepository.get { installerType }

    suspend operator fun invoke() = coroutineScope {
        setupInstaller()
        installer()
        uninstaller()
    }

    fun close() {
        _installer = null
        uninstallItems.close()
        installItems.close()
    }

    suspend infix fun install(installItem: InstallItem) {
        installItems.send(installItem)
    }

    suspend infix fun uninstall(packageName: PackageName) {
        uninstallItems.send(packageName)
    }

    infix fun remove(packageName: PackageName) {
        updateState { remove(packageName) }
    }

    private fun CoroutineScope.setupInstaller() = launch {
        installerPreference.collectLatest(::setInstaller)
    }

    private fun CoroutineScope.installer() = launch {
        val currentQueue = mutableSetOf<String>()
        installItems.filter { item ->
            currentQueue.addAndCompute(item.packageName.name) { isAdded ->
                if (isAdded) {
                    updateState { put(item.packageName, InstallState.Pending) }
                }
            }
        }.consumeEach { item ->
            if (state.value.containsKey(item.packageName)) {
                updateState { put(item.packageName, InstallState.Installing) }
                val success = installer.install(item)
                installer.cleanup()
                updateState { put(item.packageName, success) }
                context.notificationManager?.cancel(
                    "download-${item.packageName.name}",
                    Constants.NOTIFICATION_ID_DOWNLOADING
                )
                currentQueue.remove(item.packageName.name)
            }
        }
    }

    private fun CoroutineScope.uninstaller() = launch {
        uninstallItems.consumeEach {
            installer.uninstall(it)
        }
    }

    private suspend fun setInstaller(installerType: InstallerType) {
        lock.withLock {
            _installer = when (installerType) {
                InstallerType.LEGACY -> LegacyInstaller(context)
                InstallerType.SESSION -> SessionInstaller(context)
                InstallerType.SHIZUKU -> ShizukuInstaller(context)
                InstallerType.ROOT -> RootInstaller(context)
            }
        }
    }

    private inline fun updateState(block: MutableMap<PackageName, InstallState>.() -> Unit) {
        state.update { it.updateAsMutable(block) }
    }
}
