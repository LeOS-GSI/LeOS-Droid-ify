package com.leos.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.leos.core.database.utils.localizedValue
import com.leos.core.domain.newer.AntiFeature
import com.leos.core.domain.newer.Authentication
import com.leos.core.domain.newer.Category
import com.leos.core.domain.newer.Repo
import com.leos.core.domain.newer.VersionInfo
import kotlinx.serialization.Serializable

@Entity(tableName = "repos")
data class RepoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val enabled: Boolean,
    val fingerprint: String,
    val etag: String,
    val username: String,
    val password: String,
    val address: String,
    val mirrors: List<String>,
    val name: LocalizedString,
    val description: LocalizedString,
    val antiFeatures: Map<String, AntiFeatureEntity>,
    val categories: Map<String, CategoryEntity>,
    val timestamp: Long
)

fun RepoEntity.update(repo: Repo) = copy(
    username = repo.authentication.username,
    password = repo.authentication.password,
    timestamp = repo.versionInfo.timestamp,
    enabled = repo.enabled,
    mirrors = repo.mirrors,
    fingerprint = repo.fingerprint
)

fun RepoEntity.toExternal(locale: String): Repo = Repo(
    id = id!!,
    enabled = enabled,
    address = address,
    name = name.localizedValue(locale) ?: "",
    description = description.localizedValue(locale) ?: "",
    fingerprint = fingerprint,
    authentication = Authentication(username, password),
    versionInfo = VersionInfo(timestamp = timestamp, etag = etag),
    mirrors = mirrors,
    categories = categories.values.toCategoryList(locale),
    antiFeatures = antiFeatures.values.toAntiFeatureList(locale)
)

fun List<RepoEntity>.toExternal(locale: String): List<Repo> =
    map { it.toExternal(locale) }

@Serializable
data class CategoryEntity(
    val icon: LocalizedString,
    val name: LocalizedString,
    val description: LocalizedString
)

private fun CategoryEntity.toCategory(locale: String) =
    Category(
        name = name.localizedValue(locale) ?: "",
        icon = icon.localizedValue(locale) ?: "",
        description = description.localizedValue(locale) ?: ""
    )

fun Collection<CategoryEntity>.toCategoryList(locale: String): List<Category> =
    map { it.toCategory(locale) }

@Serializable
data class AntiFeatureEntity(
    val icon: LocalizedString,
    val name: LocalizedString,
    val description: LocalizedString
)

private fun AntiFeatureEntity.toAntiFeature(locale: String) =
    AntiFeature(
        name = name.localizedValue(locale) ?: "",
        icon = icon.localizedValue(locale) ?: "",
        description = description.localizedValue(locale) ?: ""
    )

fun Collection<AntiFeatureEntity>.toAntiFeatureList(locale: String): List<AntiFeature> =
    map { it.toAntiFeature(locale) }
