package com.leos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leos.core.database.dao.AppDao
import com.leos.core.database.dao.InstalledDao
import com.leos.core.database.dao.RepoDao
import com.leos.core.database.model.AppEntity
import com.leos.core.database.model.InstalledEntity
import com.leos.core.database.model.RepoEntity

@Database(
    version = 1,
    entities = [
        AppEntity::class,
        RepoEntity::class,
        InstalledEntity::class
    ]
)
@TypeConverters(
    CollectionConverter::class,
    LocalizedConverter::class,
    PackageEntityConverter::class,
    RepoConverter::class
)
abstract class DroidifyDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    abstract fun repoDao(): RepoDao

    abstract fun installedDao(): InstalledDao
}
