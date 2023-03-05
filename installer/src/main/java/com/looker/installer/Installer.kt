package com.leos.installer

import android.content.Context
import com.leos.core.common.extension.filter
import com.leos.core.datastore.UserPreferencesRepository
import com.leos.core.datastore.model.InstallerType
import com.leos.core.model.newer.PackageName
import com.leos.installer.installers.BaseInstaller
import com.leos.installer.installers.LegacyInstaller
import com.leos.installer.installers.RootInstaller
import com.leos.installer.installers.SessionInstaller
import com.leos.installer.installers.ShizukuInstaller
import com.leos.installer.model.InstallItem
import com.leos.installer.model.InstallItemState
import com.leos.installer.model.InstallState
import com.leos.installer.model.statesTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class Installer(
	private val context: Context,
	private val userPreferencesRepository: UserPreferencesRepository
) {
	private val installItems = Channel<InstallItem>()
	private val uninstallItems = Channel<PackageName>()
	private val installState = MutableStateFlow(InstallItemState.EMPTY)
	private var baseInstaller: BaseInstaller? = null

	suspend operator fun invoke() = coroutineScope {
		baseInstaller =
			when (userPreferencesRepository.fetchInitialPreferences().installerType) {
				InstallerType.LEGACY -> LegacyInstaller(context)
				InstallerType.SESSION -> SessionInstaller(context)
				InstallerType.SHIZUKU -> ShizukuInstaller(context)
				InstallerType.ROOT -> RootInstaller(context)
			}
		installer(
			baseInstaller = baseInstaller!!,
			installItems = installItems,
			installState = installState
		)
		uninstaller(
			baseInstaller = baseInstaller!!,
			uninstallItems = uninstallItems
		)

	}

	fun close() {
		baseInstaller?.cleanup()
		baseInstaller = null
		uninstallItems.close()
		installItems.close()
	}

	suspend operator fun plus(installItem: InstallItem) {
		installItems.send(installItem)
	}

	suspend operator fun minus(packageName: PackageName) {
		uninstallItems.send(packageName)
	}

	infix fun stateOf(packageName: PackageName): Flow<InstallState> = installState
		.filter { it.installedItem.packageName == packageName }
		.map { it.state }

	private fun CoroutineScope.installer(
		baseInstaller: BaseInstaller,
		installItems: ReceiveChannel<InstallItem>,
		installState: MutableStateFlow<InstallItemState>
	) = launch {
		val requested = mutableSetOf<String>()
		filter(installItems) {
			installState.emit(it statesTo InstallState.Queued)
			requested.add(it.packageName.name)
		}.consumeEach {
			installState.emit(it statesTo InstallState.Installing)
			val success = baseInstaller.performInstall(it)
			installState.emit(it statesTo success)
			requested.remove(it.packageName.name)
		}
	}

	private fun CoroutineScope.uninstaller(
		baseInstaller: BaseInstaller,
		uninstallItems: ReceiveChannel<PackageName>
	) = launch {
		uninstallItems.consumeEach {
			baseInstaller.performUninstall(it)
		}
	}
}
