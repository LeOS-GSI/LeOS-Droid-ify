package com.leos.droidify.utility

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import com.leos.core.common.extension.calculateHash
import com.leos.core.common.extension.getDrawableCompat
import com.leos.core.common.extension.singleSignature
import com.leos.core.common.extension.versionCodeCompat
import com.leos.core.model.InstalledItem
import com.leos.core.model.Product
import com.leos.core.model.Repository
import com.leos.droidify.service.Connection
import com.leos.droidify.service.DownloadService
import com.leos.droidify.utility.extension.android.Android

object Utils {

	fun PackageInfo.toInstalledItem(): InstalledItem {
		val signatureString = singleSignature?.calculateHash().orEmpty()
		return InstalledItem(
			packageName,
			versionName.orEmpty(),
			versionCodeCompat,
			signatureString
		)
	}

	fun getToolbarIcon(context: Context, resId: Int): Drawable {
		return context.getDrawableCompat(resId).mutate()
	}

	fun startUpdate(
		packageName: String,
		installedItem: InstalledItem?,
		products: List<Pair<Product, Repository>>,
		downloadConnection: Connection<DownloadService.Binder, DownloadService>,
	) {
		val productRepository = Product.findSuggested(products, installedItem) { it.first }
		val compatibleReleases = productRepository?.first?.selectedReleases.orEmpty()
			.filter { installedItem == null || installedItem.signature == it.signature }
		val release = if (compatibleReleases.size >= 2) {
			compatibleReleases
				.filter { it.platforms.contains(Android.primaryPlatform) }
				.minByOrNull { it.platforms.size }
				?: compatibleReleases.minByOrNull { it.platforms.size }
				?: compatibleReleases.firstOrNull()
		} else {
			compatibleReleases.firstOrNull()
		}
		val binder = downloadConnection.binder
		if (productRepository != null && release != null && binder != null) {
			binder.enqueue(
				packageName,
				productRepository.first.name,
				productRepository.second,
				release
			)
		}
	}
}
