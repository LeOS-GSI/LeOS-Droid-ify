package com.leos.core.data.fdroid.repository.offline

import android.content.Context
import com.leos.core.data.fdroid.model.allowUnstable
import com.leos.core.data.fdroid.model.toEntity
import com.leos.core.data.fdroid.repository.RepoRepository
import com.leos.core.data.fdroid.sync.getIndexV1
import com.leos.core.data.fdroid.sync.processRepos
import com.leos.core.database.dao.AppDao
import com.leos.core.database.dao.RepoDao
import com.leos.core.database.model.RepoEntity
import com.leos.core.database.model.toEntity
import com.leos.core.database.model.toExternalModel
import com.leos.core.model.newer.Repo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstRepoRepository @Inject constructor(
	private val appDao: AppDao,
	private val repoDao: RepoDao
) : RepoRepository {
	override fun getRepos(): Flow<List<Repo>> =
		repoDao.getRepoStream().map { it.map(RepoEntity::toExternalModel) }

	override suspend fun updateRepo(repo: Repo): Boolean = try {
		repoDao.updateRepo(repo.toEntity())
		true
	} catch (e: Exception) {
		false
	}

	override suspend fun enableRepository(repo: Repo, enable: Boolean) {
		repoDao.updateRepo(repo.copy(enabled = enable).toEntity())
	}

	override suspend fun sync(context: Context, repo: Repo, allowUnstable: Boolean): Boolean {
		TODO("Not yet implemented")
	}

	override suspend fun syncAll(context: Context, allowUnstable: Boolean): Boolean =
		coroutineScope {
			val repos = repoDao.getRepoStream().first().map(RepoEntity::toExternalModel)
				.filter { it.enabled }
			val repoChannel = Channel<Repo>()
			processRepos(context, repoChannel) { repo, jar ->
				val index = jar.getIndexV1()
				val updatedRepo = index.repo.toEntity(
					fingerPrint = repo.fingerprint,
					etag = repo.etag,
					username = repo.username,
					password = repo.password
				)
				val packages = index.packages
				val apps =
					index.apps.map {
						it.toEntity(
							repoId = repo.id,
							packages = packages[it.packageName]?.allowUnstable(it, allowUnstable)
								?: emptyList()
						)
					}
				repoDao.updateRepo(updatedRepo)
				appDao.upsertApps(apps)
			}
			repos.forEach { repoChannel.send(it) }
			false
		}
}