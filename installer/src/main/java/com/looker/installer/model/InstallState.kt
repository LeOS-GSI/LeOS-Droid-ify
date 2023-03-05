package com.leos.installer.model

sealed interface InstallState {

	object Failed : InstallState

	object Queued : InstallState

	object Installing : InstallState

	object Installed : InstallState

}