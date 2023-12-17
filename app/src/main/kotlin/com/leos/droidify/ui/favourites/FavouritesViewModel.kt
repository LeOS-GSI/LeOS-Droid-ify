package com.leos.droidify.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leos.core.common.extension.asStateFlow
import com.leos.core.datastore.SettingsRepository
import com.leos.core.datastore.get
import com.leos.core.domain.Product
import com.leos.droidify.database.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val favouriteApps: StateFlow<List<List<Product>>> =
        settingsRepository
            .get { favouriteApps }
            .map { favourites ->
                favourites.mapNotNull { app ->
                    Database.ProductAdapter.get(app, null).ifEmpty { null }
                }
            }.asStateFlow(emptyList())

    fun updateFavourites(packageName: String) {
        viewModelScope.launch {
            settingsRepository.toggleFavourites(packageName)
        }
    }
}
