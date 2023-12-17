package com.leos.sync.fdroid

import com.leos.core.domain.Syncable
import com.leos.core.domain.newer.App
import com.leos.core.domain.newer.Repo

class FdroidSyncable(override val repo: Repo) : Syncable {

    override suspend fun getApps(): List<App> = emptyList()

    override suspend fun getUpdatedRepo(): Repo = repo

}
