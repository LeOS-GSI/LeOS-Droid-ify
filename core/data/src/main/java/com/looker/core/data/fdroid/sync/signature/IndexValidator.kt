package com.leos.core.data.fdroid.sync.signature

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
import org.fdroid.index.parseV1
import org.fdroid.index.v1.IndexV1
import java.io.File

class IndexValidator(
    private val repo: Repo,
    private val fingerprintBlock: (IndexV1, String) -> Unit
) : FileValidator {
    override suspend fun validate(file: File) = withContext(Dispatchers.IO) {
        val (index, fingerprint) = getIndexAndFingerprint(file)
        if (repo.fingerprint.isNotBlank() &&
            !repo.fingerprint.equals(fingerprint, ignoreCase = true)
        ) {
            throw ValidationException(
                "Expected Fingerprint: ${repo.fingerprint}, Acquired Fingerprint: $fingerprint"
            )
        }
        fingerprintBlock(index, fingerprint)
    }

    companion object {
        const val JSON_NAME = "index-v1.json"
    }

    private suspend fun getIndexAndFingerprint(
        file: File
    ): Pair<IndexV1, String> = withContext(Dispatchers.IO) {
        val jar = file.toJarFile()
        val jarEntry = requireNotNull(jar.getJarEntry(JSON_NAME)) {
            "No entry for: $JSON_NAME"
        }

        val entry = jar
            .getInputStream(jarEntry)
            .use(IndexParser::parseV1)

        val fingerprint = jarEntry
            .codeSigner
            .certificate
            .fingerprint()
        entry to fingerprint
    }
}
