package com.leos.core.common

@JvmInline
value class PackageName(val name: String)

fun String.toPackageName() = PackageName(this)
