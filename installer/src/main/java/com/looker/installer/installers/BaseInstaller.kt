package com.leos.installer.installers

import com.leos.core.model.newer.PackageName
import com.leos.installer.model.InstallItem
import com.leos.installer.model.InstallState

interface BaseInstaller {

	suspend fun performInstall(installItem: InstallItem): InstallState

	suspend fun performUninstall(packageName: PackageName)

	fun cleanup()

}