package com.leos.droidify.database

import android.content.Context
import android.net.Uri
import com.fasterxml.jackson.core.JsonToken
import com.leos.core.common.Exporter
import com.leos.core.common.extension.Json
import com.leos.core.common.extension.forEach
import com.leos.core.common.extension.forEachKey
import com.leos.core.common.extension.parseDictionary
import com.leos.core.common.extension.writeArray
import com.leos.core.common.extension.writeDictionary
import com.leos.core.di.ApplicationScope
import com.leos.core.di.IoDispatcher
import com.leos.core.domain.Repository
import com.leos.droidify.utility.serialization.repository
import com.leos.droidify.utility.serialization.serialize
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class RepositoryExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : Exporter<List<Repository>> {
    override suspend fun export(item: List<Repository>, target: Uri) {
        scope.launch(ioDispatcher) {
            val stream = context.contentResolver.openOutputStream(target)
            Json.factory.createGenerator(stream).use { generator ->
                generator.writeDictionary {
                    writeArray("repositories") {
                        item.map {
                            it.copy(
                                id = -1,
                                mirrors = if (it.enabled) it.mirrors else emptyList(),
                                lastModified = "",
                                entityTag = ""
                            )
                        }.forEach { repo ->
                            writeDictionary {
                                repo.serialize(this)
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun import(target: Uri): List<Repository> = withContext(ioDispatcher) {
        val list = mutableListOf<Repository>()
        val stream = context.contentResolver.openInputStream(target)
        Json.factory.createParser(stream).use { generator ->
            generator?.parseDictionary {
                forEachKey {
                    if (it.array("repositories")) {
                        forEach(JsonToken.START_OBJECT) {
                            val repo = repository()
                            list.add(repo)
                        }
                    }
                }
            }
        }
        list
    }
}
