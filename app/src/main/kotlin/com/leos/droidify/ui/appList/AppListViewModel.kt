package com.leos.droidify.ui.appList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leos.core.common.extension.asStateFlow
import com.leos.core.datastore.SettingsRepository
import com.leos.core.datastore.get
import com.leos.core.datastore.model.SortOrder
import com.leos.core.domain.ProductItem
import com.leos.core.domain.ProductItem.Section.All
import com.leos.droidify.database.CursorOwner
import com.leos.droidify.database.Database
import com.leos.droidify.service.Connection
import com.leos.droidify.service.SyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class AppListViewModel
@Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val reposStream = Database.RepositoryAdapter
        .getAllStream()
        .asStateFlow(emptyList())

    val showUpdateAllButton = Database.ProductAdapter
        .getUpdatesStream()
        .map { it.isNotEmpty() }
        .asStateFlow(false)

    val sortOrderFlow = settingsRepository.get { sortOrder }
        .asStateFlow(SortOrder.UPDATED)

    private val sections = MutableStateFlow<ProductItem.Section>(All)

    val searchQuery = MutableStateFlow("")

    val syncConnection = Connection(SyncService::class.java)

    fun updateAll() {
        viewModelScope.launch {
            syncConnection.binder?.updateAllApps()
        }
    }

    fun request(source: AppListFragment.Source): CursorOwner.Request {
        return when (source) {
            AppListFragment.Source.AVAILABLE -> CursorOwner.Request.ProductsAvailable(
                searchQuery.value,
                sections.value,
                sortOrderFlow.value
            )

            AppListFragment.Source.INSTALLED -> CursorOwner.Request.ProductsInstalled(
                searchQuery.value,
                sections.value,
                sortOrderFlow.value
            )

            AppListFragment.Source.UPDATES -> CursorOwner.Request.ProductsUpdates(
                searchQuery.value,
                sections.value,
                sortOrderFlow.value
            )
        }
    }

    fun setSection(newSection: ProductItem.Section, perform: () -> Unit) {
        viewModelScope.launch {
            if (newSection != sections.value) {
                sections.emit(newSection)
                launch(Dispatchers.Main) { perform() }
            }
        }
    }

    fun setSearchQuery(newSearchQuery: String, perform: () -> Unit) {
        viewModelScope.launch {
            if (newSearchQuery != searchQuery.value) {
                searchQuery.emit(newSearchQuery)
                launch(Dispatchers.Main) { perform() }
            }
        }
    }
}
