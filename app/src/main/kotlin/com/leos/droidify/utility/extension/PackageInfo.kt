package com.leos.droidify.utility.extension

import android.content.pm.PackageInfo
import com.leos.core.common.extension.calculateHash
import com.leos.core.common.extension.singleSignature
import com.leos.core.common.extension.versionCodeCompat
import com.leos.core.domain.InstalledItem

fun PackageInfo.toInstalledItem(): InstalledItem {
    val signatureString = singleSignature?.calculateHash().orEmpty()
    return InstalledItem(
        packageName,
        versionName.orEmpty(),
        versionCodeCompat,
        signatureString
    )
}
