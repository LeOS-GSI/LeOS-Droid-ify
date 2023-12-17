package com.leos.core.data.fdroid.repository

import com.leos.core.common.PackageName
import com.leos.core.domain.newer.App
import com.leos.core.domain.newer.Author
import com.leos.core.domain.newer.Package
import kotlinx.coroutines.flow.Flow

interface AppRepository {

    fun getApps(): Flow<List<App>>

    fun getApp(packageName: PackageName): Flow<List<App>>

    fun getAppFromAuthor(author: Author): Flow<List<App>>

    fun getPackages(packageName: PackageName): Flow<List<Package>>

    /**
     * returns true is the app is added successfully
     * returns false if the app was already in the favourites and so it is removed
     */
    suspend fun addToFavourite(packageName: PackageName): Boolean
}
