pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven(url = "https://jitpack.io")
	}
}

rootProject.name = "LeOS-Droid"
include(
	":app",
	":core:common",
	":core:data",
	":core:database",
	":core:datastore",
	":core:model",
	":feature-settings",
	":installer"
)
