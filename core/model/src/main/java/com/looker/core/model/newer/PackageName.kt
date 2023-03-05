package com.leos.core.model.newer

@JvmInline
value class PackageName(val name: String)

fun String.toPackageName() = PackageName(this)