package com.leos.installer.model

import com.leos.core.common.PackageName
import com.leos.core.common.toPackageName

data class InstallItem(
    val packageName: PackageName,
    val installFileName: String
)

infix fun String.installFrom(fileName: String) = InstallItem(this.toPackageName(), fileName)
