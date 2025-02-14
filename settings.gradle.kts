pluginManagement {
	includeBuild("build-logic")
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
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
    ":core:di",
    ":core:domain",
	":core:network",
	":installer"
)
include(":sync:fdroid")
