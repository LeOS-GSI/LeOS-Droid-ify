package com.leos.core.domain

import com.leos.core.domain.newer.App
import com.leos.core.domain.newer.Repo

interface Syncable {

    val repo: Repo

    suspend fun getApps(): List<App>

    suspend fun getUpdatedRepo(): Repo

}
