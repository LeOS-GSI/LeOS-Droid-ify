package com.leos.core.data.fdroid.repository.offline

import com.leos.core.data.fdroid.repository.AppRepository
import com.leos.core.database.dao.AppDao
import com.leos.core.database.model.AppEntity
import com.leos.core.database.model.PackageEntity
import com.leos.core.database.model.toExternalModel
import com.leos.core.datastore.UserPreferencesRepository
import com.leos.core.model.newer.App
import com.leos.core.model.newer.Author
import com.leos.core.model.newer.Package
import com.leos.core.model.newer.PackageName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstAppRepository @Inject constructor(
	private val appDao: AppDao,
	private val userPreferencesRepository: UserPreferencesRepository
) : AppRepository {
	override fun getApps(): Flow<List<App>> =
		appDao.getAppStream().map { it.map(AppEntity::toExternalModel) }

	override fun getApp(packageName: PackageName): Flow<List<App>> =
		appDao.getApp(packageName.name).map { it.map(AppEntity::toExternalModel) }

	override fun getAppFromAuthor(author: Author): Flow<List<App>> =
		appDao.getAppsFromAuthor(author.name).map { it.map(AppEntity::toExternalModel) }

	override fun getPackages(packageName: PackageName): Flow<List<Package>> =
		appDao.getPackages(packageName.name).map { it.map(PackageEntity::toExternalModel) }

	override suspend fun addToFavourite(packageName: PackageName): Boolean {
		val isFavourite =
			userPreferencesRepository
				.fetchInitialPreferences()
				.favouriteApps
				.any { it == packageName.name }
		userPreferencesRepository.toggleFavourites(packageName.name)
		return !isFavourite
	}
}