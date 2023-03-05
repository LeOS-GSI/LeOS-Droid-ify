package com.leos.droidify.ui.app_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leos.core.datastore.UserPreferencesRepository
import com.leos.core.datastore.distinctMap
import com.leos.core.datastore.model.SortOrder
import com.leos.core.model.ProductItem
import com.leos.droidify.database.CursorOwner
import com.leos.droidify.service.Connection
import com.leos.droidify.service.SyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel
@Inject constructor(
	userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

	private val sortOrderFlow = userPreferencesRepository
		.userPreferencesFlow
		.distinctMap { it.sortOrder }

	private val _sections = MutableStateFlow<ProductItem.Section>(ProductItem.Section.All)
	private val _searchQuery = MutableStateFlow("")

	private val sections: StateFlow<ProductItem.Section> = _sections.stateIn(
		initialValue = ProductItem.Section.All,
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000)
	)
	val searchQuery: StateFlow<String> = _searchQuery.stateIn(
		initialValue = "",
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000)
	)

	val syncConnection = Connection(SyncService::class.java)

	fun updateAll() {
		viewModelScope.launch {
			syncConnection.binder?.updateAllApps()
		}
	}

	fun request(source: AppListFragment.Source): CursorOwner.Request {
		var mSearchQuery = ""
		var mSections: ProductItem.Section = ProductItem.Section.All
		var mOrder: SortOrder = SortOrder.NAME
		viewModelScope.launch {
			launch { searchQuery.collect { mSearchQuery = it } }
			launch { sections.collect { if (source.sections) mSections = it } }
			sortOrderFlow.collect { mOrder = it }
		}
		return when (source) {
			AppListFragment.Source.AVAILABLE -> CursorOwner.Request.ProductsAvailable(
				mSearchQuery,
				mSections,
				mOrder
			)
			AppListFragment.Source.INSTALLED -> CursorOwner.Request.ProductsInstalled(
				mSearchQuery,
				mSections,
				mOrder
			)
			AppListFragment.Source.UPDATES -> CursorOwner.Request.ProductsUpdates(
				mSearchQuery,
				mSections,
				mOrder
			)
		}
	}

	fun setSection(newSection: ProductItem.Section, perform: () -> Unit) {
		viewModelScope.launch {
			if (newSection != sections.value) {
				_sections.emit(newSection)
				launch(Dispatchers.Main) { perform() }
			}
		}
	}

	fun setSearchQuery(newSearchQuery: String, perform: () -> Unit) {
		viewModelScope.launch {
			if (newSearchQuery != searchQuery.value) {
				_searchQuery.emit(newSearchQuery)
				launch(Dispatchers.Main) { perform() }
			}
		}
	}
}