package com.leos.core.data.fdroid.repository.offline

import com.leos.core.common.extension.exceptCancellation
import com.leos.core.data.fdroid.repository.RepoRepository
import com.leos.core.data.fdroid.sync.IndexManager
import com.leos.core.data.fdroid.toEntity
import com.leos.core.database.dao.AppDao
import com.leos.core.database.dao.RepoDao
import com.leos.core.database.model.toExternal
import com.leos.core.database.model.update
import com.leos.core.datastore.SettingsRepository
import com.leos.core.di.ApplicationScope
import com.leos.core.di.DefaultDispatcher
import com.leos.core.domain.newer.Repo
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class OfflineFirstRepoRepository @Inject constructor(
    private val appDao: AppDao,
    private val repoDao: RepoDao,
    private val settingsRepository: SettingsRepository,
    private val indexManager: IndexManager,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) : RepoRepository {

    private val preference = runBlocking {
        settingsRepository.getInitial()
    }

    private val locale = preference.language

    override suspend fun getRepo(id: Long): Repo = withContext(dispatcher) {
        repoDao.getRepoById(id).toExternal(locale)
    }

    override fun getRepos(): Flow<List<Repo>> =
        repoDao.getRepoStream().map { it.toExternal(locale) }

    override suspend fun updateRepo(repo: Repo) {
        scope.launch {
            val entity = repoDao.getRepoById(repo.id)
            repoDao.upsertRepo(entity.update(repo))
        }
    }

    override suspend fun enableRepository(repo: Repo, enable: Boolean) {
        scope.launch {
            val entity = repoDao.getRepoById(repo.id)
            repoDao.upsertRepo(entity.copy(enabled = enable))
            if (enable) sync(repo)
        }
    }

    override suspend fun sync(repo: Repo): Boolean = coroutineScope {
        val index = try {
            indexManager.getIndex(listOf(repo))[repo] ?: throw Exception("Empty index returned")
        } catch (e: Exception) {
            e.exceptCancellation()
            return@coroutineScope false
        }
        val updatedRepo = index.repo.toEntity(
            id = repo.id,
            fingerprint = repo.fingerprint,
            username = repo.authentication.username,
            password = repo.authentication.password,
            etag = repo.versionInfo.etag ?: "",
            enabled = true
        )
        repoDao.upsertRepo(updatedRepo)
        val apps = index.packages.map {
            it.value.toEntity(it.key, repo.id, preference.unstableUpdate)
        }
        appDao.upsertApps(apps)
        true
    }

    override suspend fun syncAll(): Boolean = supervisorScope {
        val repos = repoDao.getRepoStream().first().filter { it.enabled }
        val indices = try {
            indexManager
                .getIndex(repos.toExternal(locale))
                .filter { (_, index) -> index != null }
        } catch (e: Exception) {
            e.exceptCancellation()
            return@supervisorScope false
        }
        if (indices.isEmpty()) return@supervisorScope true
        indices.forEach { (repo, index) ->
            val updatedRepo = index!!.repo.toEntity(
                id = repo.id,
                fingerprint = repo.fingerprint,
                username = repo.authentication.username,
                password = repo.authentication.password,
                etag = repo.versionInfo.etag ?: "",
                enabled = true
            )
            repoDao.upsertRepo(updatedRepo)
            val apps = index.packages.map {
                it.value.toEntity(it.key, repo.id, preference.unstableUpdate)
            }
            appDao.upsertApps(apps)
        }
        true
    }
}
