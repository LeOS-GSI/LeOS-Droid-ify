package com.leos.installer.installers

import com.leos.core.common.PackageName
import com.leos.installer.model.InstallItem
import com.leos.installer.model.InstallState

interface Installer {

    suspend fun install(installItem: InstallItem): InstallState

    suspend fun uninstall(packageName: PackageName)

    fun cleanup()
}
