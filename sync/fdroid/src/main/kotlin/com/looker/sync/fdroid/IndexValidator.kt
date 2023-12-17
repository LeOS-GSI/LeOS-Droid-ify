package com.leos.sync.fdroid

import com.leos.core.common.extension.certificate
import com.leos.core.common.extension.codeSigner
import com.leos.core.common.extension.fingerprint
import com.leos.core.common.extension.toJarFile
import com.leos.core.common.signature.FileValidator
import com.leos.core.common.signature.ValidationException
import com.leos.core.domain.newer.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fdroid.index.IndexParser
import org.fdroid.index.parseEntry
import org.fdroid.index.parseV1
import org.fdroid.index.v1.IndexV1
import org.fdroid.index.v2.Entry
import java.io.File
import java.io.InputStream

class IndexValidator<T>(
    private val repo: Repo,
    private val config: ValidatorConfig<T>,
    private val block: (T, String) -> Unit
) : FileValidator {
    override suspend fun validate(file: File) = withContext(Dispatchers.Default) {
        val (entry, fingerprint) = getEntryStream(file, config.parser, config.jsonName)
        if (repo.fingerprint.isNotBlank() &&
            !repo.fingerprint.equals(fingerprint, ignoreCase = true)
        ) {
            throw ValidationException(
                "Expected Fingerprint: ${repo.fingerprint}, " +
                    "Acquired Fingerprint: $fingerprint"
            )
        }
        block(entry, fingerprint)
    }

    companion object {
        private suspend fun <T> getEntryStream(
            file: File,
            getIndexValue: (InputStream) -> T,
            entryName: String
        ): Pair<T, String> = withContext(Dispatchers.IO) {
            val jar = file.toJarFile()
            val jarEntry = jar.getJarEntry(entryName)
                ?: throw ValidationException("No entry for: $entryName")

            val entry = jar
                .getInputStream(jarEntry)
                .use(getIndexValue)

            val fingerprint = jarEntry
                .codeSigner
                .certificate
                .fingerprint()
            entry to fingerprint
        }
    }
}

sealed class ValidatorConfig<T>(
    val jsonName: String,
    val parser: (InputStream) -> T
) {

    data object EntryConfig : ValidatorConfig<Entry>("entry.json", IndexParser::parseEntry)

    data object IndexConfig : ValidatorConfig<IndexV1>("index-v1.json", IndexParser::parseV1)

}
